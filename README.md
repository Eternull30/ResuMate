# ResuMate - AI-Powered Resume Builder

<div align="center">

![ResuMate](https://img.shields.io/badge/Android-Resume%20Builder-green?style=flat-square&logo=android)
![Status](https://img.shields.io/badge/Status-Active-brightgreen?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)
![Android](https://img.shields.io/badge/Android-API%2030%2B-green?style=flat-square)

**Transform your resume with AI-powered enhancements**

</div>

---

## Overview

**ResuMate** is a modern Android resume builder application that leverages Google's Gemini AI to help users create professional, polished resumes. The app provides an intuitive interface for resume creation, AI-powered content improvement, multiple resume templates, and PDF export capabilities.

Whether you're a first-time job seeker or an experienced professional, ResuMate makes it easy to build, enhance, and download your resume in minutes.

---

## Features

### AI-Powered Enhancement
- **Gemini AI Integration**: Use Google's advanced language model to improve your resume content
- **Full Resume Optimization**: Enhance professional summary, education, skills, experience, and projects all at once
- **Smart Text Improvement**: AI rewrites your content with better action verbs, quantifiable metrics, and professional language

### Resume Management
- **Create Multiple Resumes**: Build and save multiple versions of your resume
- **Edit Anytime**: Update and modify your resume with ease
- **Cloud Sync**: All resumes backed up to Firebase Firestore

### Multiple Templates
- **Modern Template**: Clean, minimalist design
- **Professional Template**: Traditional, formal style
- **Creative Template**: Bold, modern approach
- **Template Selection**: Choose your preferred style before export

### Export & Download
- **PDF Generation**: Download your resume as a professional PDF
- **Downloads Folder**: Saved automatically to your device's Downloads folder
- **Multiple Formats**: Support for different resume styles

### User Authentication
- **Firebase Authentication**: Secure email/password login
- **Cloud Storage**: Firestore integration for data persistence
- **Private Resumes**: All your data is private and secure

### Sections Included
- Full Name, Email, Phone
- Professional Summary
- Education
- Technical Skills
- Relevant Experience
- Projects
- Customizable layouts

---

## Tech Stack

### Frontend
- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern UI framework
- **Material Design 3** - Material Design components

### Backend & Services
- **Firebase Authentication** - User authentication
- **Cloud Firestore** - Real-time database
- **Google Gemini API** - AI content enhancement
- **Retrofit** - HTTP client for API calls
- **OkHttp** - Network layer

### Libraries & Tools
- **Jetpack** - Navigation, ViewModel, Compose
- **Gson** - JSON serialization
- **Android PDF Document** - PDF generation
- **Coroutines** - Asynchronous programming

---

## Getting Started

### Prerequisites
- Android 11.0 (API 30) or higher
- Google Play Services
- Internet connection

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/ResuMate.git
   cd ResuMate
   ```

2. **Set up Firebase**
   - Create a Firebase project at [firebase.google.com](https://firebase.google.com)
   - Download `google-services.json` and place it in the `app/` directory
   - Enable Authentication (Email/Password)
   - Enable Firestore Database

3. **Get Gemini API Key**
   - Visit [Google AI Studio](https://aistudio.google.com/apikey)
   - Create a new API key
   - Create `local.properties` in project root:
     ```
     GEMINI_API_KEY=your_api_key_here
     ```

4. **Build and Run**
   ```bash
   ./gradlew build
   # Then run on emulator or device
   ```

---

## 📖 Usage

### Creating a Resume

1. **Launch the app** and sign up or log in
2. **Create a new resume** with a title and template selection
3. **Fill in your information**:
   - Personal details (name, email, phone)
   - Professional summary
   - Education
   - Technical skills
   - Work experience
   - Projects

### Enhancing with AI

1. **Click "✨ Improve All with AI"** button
2. **Wait 10-30 seconds** for Gemini to enhance your content
3. **Review the improvements** - your text will be more professional and impactful
4. **Save your changes** when satisfied

### Downloading PDF

1. **Select your template** (Modern, Professional, or Creative)
2. **Click "📥 Download PDF"**
3. **Find your resume** in Downloads/ResumeBuilder folder
4. **Share with employers** via email or job portals

---

##  API Configuration

### Firebase Setup
```kotlin
// Already configured in the app
// Just add google-services.json to the app/ directory
```

### Gemini API
```kotlin
// In local.properties
GEMINI_API_KEY=your_api_key_here

// Used in GeminiService.kt
private val apiKey = BuildConfig.GEMINI_API_KEY
```

### Timeout Configuration
```kotlin
// OkHttp client with 60-second timeout for Gemini API
private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()
```

---

## Architecture

ResuMate follows the **Clean Architecture** pattern with clear separation of concerns:

```
app/
├── data/
│   ├── remote/
│   │   └── gemini/          # Gemini API integration
│   └── local/               # Local database
├── domain/
│   ├── model/               # Data models
│   └── repository/          # Repository interfaces
├── ui/
│   ├── screen/              # Compose screens
│   └── theme/               # Material Design theme
├── viewmodel/               # ViewModels
└── utils/                   # Utilities (PDF generation, etc.)
```

---

## Security

- **API Key Protection**: API key stored in `local.properties` (not committed)
- **Secure Authentication**: Firebase handles password encryption
- **Private Data**: All user resumes are stored securely in Firestore
- **No Sensitive Logging**: Removed API key logging in production

---

## Roadmap

- [ ] Multi-language support
- [ ] Resume templates customization
- [ ] LinkedIn profile import
- [ ] Job application tracker
- [ ] Cover letter generation
- [ ] Resume analytics (ATS score)
- [ ] Dark mode
- [ ] Offline mode

---

## Contributing

Contributions are welcome! Here's how you can help:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

<div align="center">

Developed with ❤️ by Jeet Tanwar

⭐ If you find this project helpful, please star it!

</div>
