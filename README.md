# 🤖 Robo-Advisor Service (Investment Counseling & Analysis) - Backend

**Robo-Advisor Service**는 사용자의 투자 결정을 돕기 위해 실시간 주식 데이터 분석과 AI 기반 맞춤형 상담을 제공하는 비동기 기반 백엔드 시스템입니다.

## 📝 프로젝트 개요
대규모 트래픽과 외부 API 연동 시의 병목 현상을 해결하기 위해 **Spring WebFlux**를 도입하여 논블로킹(Non-blocking) 아키텍처를 구현했습니다. 실시간 시장 데이터 수집, 기술적 지표 산출, 그리고 AI 챗봇과의 유기적인 연동을 통해 사용자에게 전문적인 투자 인사이트를 제공합니다.

## ✨ 핵심 기능 (Key Features)

### 1. 비동기 AI 투자 상담 시스템
* **High Performance WebClient**: OpenAI 등 외부 AI 서비스와의 통신 시 `WebClient`를 활용하여 요청 당 스레드 점유를 최소화하고 시스템 응답성을 극대화했습니다.
* **Real-time Chatting**: 지연 없는 투자 상담 시나리오를 지원하기 위해 비동기 메시지 스트림 아키텍처를 설계했습니다.

### 2. 기술적 분석 및 주식 지표 시스템
* **Technical Indicator Engine**: `ta4j` 라이브러리를 연동하여 RSI, MACD, MA20 등 핵심 주식 기술 지표를 정밀하게 계산합니다.
* **Financial Data Integration**: Yahoo Finance API를 통해 실시간/과거 주가 데이터를 수집하고 이를 분석 가능한 데이터 구조로 변환합니다.

### 3. 고성능 인프라 및 세션 관리
* **Distributed Session & Caching**: Redis를 도입하여 분산 환경에서도 안정적인 세션 유지와 반복적인 데이터 요청에 대한 캐싱을 수행합니다.
* **Spring Session Redis**: 다중 인스턴스 환경에서 데이터 정합성을 보장하는 중앙 집중형 세션 관리를 구현했습니다.

## 🛠 기술 스택 (Tech Stack)

* **Language**: Java 17
* **Framework**: Spring Boot 3.x, **Spring WebFlux**
* **Communication**: Spring WebClient (Non-blocking API calls)
* **Analysis**: ta4j (Technical Analysis Library)
* **Data Sources**: Yahoo Finance API
* **Database & Cache**: MySQL, **Redis**
* **Security**: Spring Security, Spring Session Redis

## 📂 주요 패키지 구조
```text
com.shieldus.roboadvisor
├── ai          # AI 챗봇 연동 및 상담 로직 (WebClient 활용)
├── analysis    # ta4j 기반 기술적 지표 계산 엔진
├── stock       # Yahoo Finance API 연동 및 주식 데이터 처리
├── config      # WebFlux, Redis, Security 등 비동기 환경 설정
└── session     # Redis 기반 분산 세션 관리 로직
