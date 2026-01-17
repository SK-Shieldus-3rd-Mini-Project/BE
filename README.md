# 🤖 Robo-Advisor Service (Investment Counseling & Analysis) - Backend

**Robo-Advisor Service**는 사용자의 투자 결정을 돕기 위해 실시간 주식 데이터 분석과 AI 기반 맞춤형 상담을 제공하는 비동기 기반 백엔드 시스템입니다.

## 📋 목차

- [개요](#개요)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [주요 기능](#주요-기능)
- [API 엔드포인트](#api-엔드포인트)
- [설치 및 실행](#설치-및-실행)
- [환경 변수](#환경-변수)

---

## 개요

**Robo-Advisor Service**는 대규모 트래픽과 외부 API 연동 시의 병목 현상을 해결하기 위해 **Spring WebFlux**를 도입하여 논블로킹(Non-blocking) 아키텍처를 구현했습니다. 실시간 시장 데이터 수집, 기술적 지표 산출, 그리고 AI 챗봇과의 유기적인 연동을 통해 사용자에게 전문적인 투자 인사이트를 제공합니다.

---

## 기술 스택

| 구분 | 기술 |
|------|------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.3.3 (**Spring WebFlux**) |
| **Database** | MariaDB / MySQL |
| **Cache & Session** | **Redis** (Spring Session Redis) |
| **Communication** | Spring WebClient (Non-blocking API calls) |
| **Analysis Engine** | **ta4j** (Technical Analysis Library) |
| **External APIs** | Yahoo Finance, DeepSearch, OpenAI |

---

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

---

## 🛠 기술 스택 (Tech Stack)

* **Language**: Java 17
* **Framework**: Spring Boot 3.x, **Spring WebFlux**
* **Communication**: Spring WebClient (Non-blocking API calls)
* **Analysis**: ta4j (Technical Analysis Library)
* **Data Sources**: Yahoo Finance API
* **Database & Cache**: MySQL, **Redis**
* **Security**: Spring Security, Spring Session Redis

---

## 프로젝트 구조

```text
src/main/java/com/roboadvisor/jeonbongjun/
├── JeonbongjunApplication.java      # 메인 애플리케이션
├── config/                          # 비동기 및 인프라 설정 (WebFlux, Redis, WebClient)
├── controller/                      # REST API 컨트롤러 (Mono/Flux 반환)
│   ├── ChatController.java          # AI 상담 비동기 엔드포인트
│   ├── StockController.java         # 주식 검색 및 데이터 제공
│   └── EconomicIndicatorController.java # 경제 지표 조회
├── service/                         # 핵심 비즈니스 로직
│   ├── ChatService.java             # AI 대화 엔진 및 히스토리 관리
│   ├── StockDetailService.java      # ta4j 기반 지표 산출 로직
│   └── YahooFinanceService.java     # 실시간 금융 데이터 래퍼
├── entity/                          # JPA 엔티티 (Stock, User, Portfolio 등)
├── repository/                      # 데이터 액세스 인터페이스
└── dto/                             # 계층 간 데이터 전송 객체
```
---

## 🔗 API 엔드포인트

### AI 상담 API (`/ai/api/chat`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/ask` | AI 투자 상담 질문 전송 (Mono<AiResponseDto>) |
| GET | `/history/{sessionId}` | 특정 세션의 이전 상담 이력 조회 |

### 시장 분석 API (`/ai/api`)
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/stock/search` | 종목명 또는 코드를 이용한 실시간 검색 |
| GET | `/news/latest` | DeepSearch 기반 최신 주요 금융 뉴스 조회 |


---

## ⚙️ 설치 및 실행

### 사전 요구사항
* **Java 17** 이상
* **Maven 3.x**
* **Redis** 서버 (세션 관리 및 캐싱용)
* **MySQL** 또는 **MariaDB**

### 로컬 실행
1.  **저장소 클론**
    ```bash
    git clone [https://github.com/SK-Shieldus-3rd-Mini-Project/be.git](https://github.com/SK-Shieldus-3rd-Mini-Project/be.git)
    cd be
    ```

2.  **빌드**
    ```bash
    ./mvnw clean install
    ```

3.  **애플리케이션 실행**
    ```bash
    ./mvnw spring-boot:run
    ```
---

## 🔐 환경 변수

`src/main/resources/application.properties` 파일 또는 시스템 환경 변수에 아래 항목을 반드시 구성해야 정상적으로 동작합니다.

| 변수명 | 설명 | 비고 |
|--------|------|------|
| `OPENAI_API_KEY` | 상담용 AI API 키 | **필수** |
| `DEEPSEARCH_API_KEY` | 뉴스 데이터 API 키 | **필수** |
| `SPRING_DATA_REDIS_HOST` | Redis 접속 호스트 | 기본값: localhost |
