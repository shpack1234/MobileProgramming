# 냉장고+ (fridgeplus)
<img src="https://img.shields.io/badge/Android-34A853?style=for-the-badge&logo=android&logoColor=white"> <img src="https://img.shields.io/badge/Java-ED8106?style=for-the-badge&logo=openjdk&logoColor=white"> <img src="https://img.shields.io/badge/ASP.NET-512BD4?style=for-the-badge&logo=blazor&logoColor=white"> <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=Docker&logoColor=white"> 

**2024년 창원대학교 컴퓨터공학과 모바일프로그래밍 팀프로젝트**

> [!NOTE]
> **이 프로젝트의 서버 구현은 다음 레포지토리를 참조하십시오.**
> 
> [Coppermine-SP/fridgeplus_server](https://github.com/Coppermine-SP/fridgeplus_server)

### Table of Content
  - [Overview](#overview)
  - [Dependencies](#dependencies)
  - [Configuration](#configuration)
    
## Overview
- Scalable 3-Tier Architecture
- Implemented with Dependency Injection patterns
- Asynchronous Server I/O
- Authenticate with a backend server using Google SSO

## Dependencies
- **libs.jackson.core**
- **libs.jackson.databind**
- **libs.jackson.annotations**
- **libs.jackson.datatype.jsr310**
- **libs.okhttp.urlconnection**
- **libs.hilt.android**
- **libs.hilt.compiler**
- **libs.googleid**
- **libs.appcompat**
- **libs.material**
- **libs.activity**
- **libs.constraintlayout**
- **libs.play.services.auth**
- **libs.credentials**
- **libs.credentials.play.services.auth**
- **libs.lottie.compose**

## Configuration
> [!NOTE]
> **Google OAuth 2.0 Client ID는 아래 Google Cloud Console 페이지에서 발급 할 수 있습니다.**
> 
> https://console.cloud.google.com/apis/credentials

> [!WARNING]
> **Google OAuth Client에 등록된 SHA-1 Fingerprint와 빌드 환경의 Keystore가 일치해야 합니다.**
>
> 앱 서명과 관련 한 자세한 정보는 다음 [Android Developers](https://developer.android.com/studio/publish/app-signing?hl=en) 페이지를 참조하십시오:

> [!WARNING]
> **API 엔드포인트 연결은 반드시 신뢰할 수 있는 HTTPS 연결을 사용하십시오.**
>
> API 서버로 사용자의 고유 식별 정보가 전송될 수 있기 때문에 반드시 안전한 HTTPS 연결을 사용해야 합니다.

프로젝트 루트 폴더에 secrets.properties 파일을 생성하고 다음과 같이 구성하십시오:
```properties
API_ENDPOINT=[API 엔드포인트]
GOOGLE_CLIENT_ID=[Google OAuth Client ID]
```
