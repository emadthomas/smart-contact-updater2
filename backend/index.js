const express = require('express');
const multer = require('multer');
const cors = require('cors');
const path = require('path');
const fs = require('fs');
const ffmpeg = require('fluent-ffmpeg');
const axios = require('axios');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 3000;
const API_KEY = process.env.API_KEY || 'default-secret-key';

// Choose which API to use
const USE_HUGGINGFACE = process.env.USE_HUGGINGFACE === 'true';
const USE_ASSEMBLYAI = process.env.USE_ASSEMBLYAI === 'true';
const HUGGINGFACE_TOKEN = process.env.HUGGINGFACE_TOKEN;
const ASSEMBLYAI_API_KEY = process.env.ASSEMBLYAI_API_KEY;

// Setup multer for file uploads
const upload = multer({
  dest: 'uploads/',
  limits: { fileSize: 50 * 1024 * 1024 }
});

// Middleware
app.use(cors());
app.use(express.json());

// API Key validation middleware
const validateApiKey = (req, res, next) => {
  const apiKey = req.headers['x-api-key'];
  if (!apiKey || apiKey !== API_KEY) {
    return res.status(401).json({ error: 'Unauthorized: Invalid API key' });
  }
  next();
};

// Convert audio to proper format
const convertAudio = (inputPath, outputPath, format = 'mp3') => {
  return new Promise((resolve, reject) => {
    ffmpeg(inputPath)
      .audioCodec(format === 'mp3' ? 'libmp3lame' : 'pcm_s16le')
      .audioBitrate('128k')
      .audioChannels(1)
      .audioFrequency(16000)
      .format(format)
      .on('end', () => resolve(outputPath))
      .on('error', (err) => reject(err))
      .save(outputPath);
  });
};

// Hugging Face Whisper API (FREE!)
const transcribeWithHuggingFace = async (audioFilePath) => {
  try {
    const audioData = fs.readFileSync(audioFilePath);
    
    const headers = { 'Content-Type': 'audio/wav' };
    if (HUGGINGFACE_TOKEN) {
      headers['Authorization'] = `Bearer ${HUGGINGFACE_TOKEN}`;
    }

    const response = await axios.post(
      'https://api-inference.huggingface.co/models/openai/whisper-large-v3',
      audioData,
      { headers, maxBodyLength: Infinity, timeout: 60000 }
    );

    return {
      transcription: response.data.text || '',
      words: [],
      provider: 'huggingface-whisper'
    };
  } catch (error) {
    if (error.response?.status === 503) {
      console.log('Model loading, waiting 20 seconds...');
      await new Promise(resolve => setTimeout(resolve, 20000));
      return transcribeWithHuggingFace(audioFilePath);
    }
    throw error;
  }
};

// AssemblyAI (5 hours/month free)
const transcribeWithAssemblyAI = async (audioFilePath) => {
  try {
    const audioData = fs.readFileSync(audioFilePath);
    
    const uploadResponse = await axios.post(
      'https://api.assemblyai.com/v2/upload',
      audioData,
      {
        headers: {
          'authorization': ASSEMBLYAI_API_KEY,
          'content-type': 'application/octet-stream'
        }
      }
    );

    const transcriptResponse = await axios.post(
      'https://api.assemblyai.com/v2/transcript',
      { audio_url: uploadResponse.data.upload_url, language_code: 'en' },
      { headers: { 'authorization': ASSEMBLYAI_API_KEY } }
    );

    const transcriptId = transcriptResponse.data.id;
    let attempts = 0;
    
    while (attempts < 30) {
      const statusResponse = await axios.get(
        `https://api.assemblyai.com/v2/transcript/${transcriptId}`,
        { headers: { 'authorization': ASSEMBLYAI_API_KEY } }
      );

      if (statusResponse.data.status === 'completed') {
        return {
          transcription: statusResponse.data.text || '',
          words: statusResponse.data.words || [],
          provider: 'assemblyai'
        };
      } else if (statusResponse.data.status === 'error') {
        throw new Error('Transcription failed');
      }

      await new Promise(resolve => setTimeout(resolve, 2000));
      attempts++;
    }

    throw new Error('Transcription timeout');
  } catch (error) {
    throw error;
  }
};

// Main transcription endpoint
app.post('/transcribe', validateApiKey, upload.single('file'), async (req, res) => {
  let uploadedFilePath = null;
  let convertedFilePath = null;

  try {
    if (!req.file) {
      return res.status(400).json({ error: 'No file uploaded' });
    }

    uploadedFilePath = req.file.path;
    const fileName = `${path.parse(req.file.originalname).name}_converted`;
    let result;
    
    if (USE_ASSEMBLYAI && ASSEMBLYAI_API_KEY) {
      console.log('Using AssemblyAI...');
      convertedFilePath = path.join('uploads', `${fileName}.mp3`);
      await convertAudio(uploadedFilePath, convertedFilePath, 'mp3');
      result = await transcribeWithAssemblyAI(convertedFilePath);
    } else if (USE_HUGGINGFACE) {
      console.log('Using Hugging Face Whisper (FREE)...');
      convertedFilePath = path.join('uploads', `${fileName}.wav`);
      await convertAudio(uploadedFilePath, convertedFilePath, 'wav');
      result = await transcribeWithHuggingFace(convertedFilePath);
    } else {
      return res.status(500).json({ 
        error: 'No transcription service configured'
      });
    }

    res.json({
      transcript: result.transcription.toLowerCase(),
      words: result.words,
      provider: result.provider
    });

  } catch (error) {
    console.error('Error:', error);
    res.status(500).json({ 
      error: 'Failed to process audio', 
      message: error.message 
    });
  } finally {
    if (uploadedFilePath && fs.existsSync(uploadedFilePath)) {
      fs.unlinkSync(uploadedFilePath);
    }
    if (convertedFilePath && fs.existsSync(convertedFilePath)) {
      fs.unlinkSync(convertedFilePath);
    }
  }
});

// Health check endpoint
app.get('/health', (req, res) => {
  res.json({ 
    status: 'ok', 
    timestamp: new Date().toISOString(),
    services: {
      huggingface: USE_HUGGINGFACE,
      assemblyai: USE_ASSEMBLYAI && !!ASSEMBLYAI_API_KEY
    }
  });
});

// Create uploads directory if it doesn't exist
if (!fs.existsSync('uploads')) {
  fs.mkdirSync('uploads');
}

app.listen(PORT, () => {
  console.log(`🚀 Server running on port ${PORT}`);
  console.log(`🎤 Services: ${USE_HUGGINGFACE ? 'HuggingFace ✅' : ''} ${USE_ASSEMBLYAI ? 'AssemblyAI ✅' : ''}`);
  console.log('Ready!');
});
