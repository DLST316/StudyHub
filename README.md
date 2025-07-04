![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6.2-green)
![JPA](https://img.shields.io/badge/JPA-Hibernate-orange)
![MySQL](https://img.shields.io/badge/DB-MySQL-yellow)
![H2](https://img.shields.io/badge/DB-H2-lightgrey)
![Thymeleaf](https://img.shields.io/badge/View-Thymeleaf-lightgrey)
![Gradle](https://img.shields.io/badge/Build-Gradle-blue)
![Cloudinary](https://img.shields.io/badge/Cloud-Cloudinary-lightblue)

# 🧠 StudyHub (스터디/취준생 커뮤니티)

스터디 모집과 취업 준비생을 위한 커뮤니티 플랫폼입니다.  
Spring Boot 기반 MVC 패턴으로 제작되었으며, 회원 관리, 스터디 기능, 게시판, 관리자 기능까지 포함합니다.

[배포 사이트 바로가기](http://studyhub-env.eba-qnsmv3is.ap-northeast-2.elasticbeanstalk.com/)

---

## 🚀 프로젝트 개요

| 항목    | 내용                                                                       |
|-------|--------------------------------------------------------------------------|
| 프로젝트명 | StudyHub                                                                 |
| 목적    | 스터디 모집 및 취준생 커뮤니티 서비스                                                    |
| 대상    | 백엔드 개발 취업용 포트폴리오                                                         |
| 기술 스택 | Java 17, Spring Boot 3.5, Spring Security 6.2, JPA/Hibernate, MySQL/H2, Thymeleaf, Gradle |
| 클라우드 서비스 | Cloudinary (이미지 업로드)                                              |
| 배포 환경 | AWS EC2                                     |

---

## 🧩 주요 기능

### 🔐 회원 관리
- **회원가입/로그인/로그아웃**
  - 사용자명 기반 로그인 시스템
  - BCrypt 비밀번호 암호화
  - Spring Security 기반 인증/인가
- **학력 정보 관리**
  - 학력 상태: 고등학교 졸업, 재학중, 졸업, 중퇴, 휴학
  - 대학교명, 전공 정보 등록
  - 이메일은 선택사항 (연락처용)
- **사용자 차단 기능**
  - 관리자가 사용자 차단/차단해제 가능
  - 차단된 사용자는 서비스 이용 제한

### 📚 스터디 관리
- **스터디 개설/수정/삭제**
  - 스터디 제목, 설명, 모집 인원 제한
  - 모집 조건 (전공, 학교, 기타 조건)
  - 모집 마감일 설정
  - 스터디장만 수정/삭제 가능
- **스터디 신청/승인 시스템**
  - 사용자별 스터디 신청 (중복 신청 방지)
  - 신청 상태: 대기중, 승인됨, 거절됨
  - 스터디장이 신청자 승인/거절 처리
  - 승인된 멤버만 댓글 작성 가능
- **스터디 검색 및 목록**
  - 제목 키워드 검색
  - 페이징 처리
  - 최신순 정렬
  - 활성 스터디 (마감일 미도과) 필터링

### 📝 커뮤니티 (게시판)
- **게시글 관리**
  - 게시글 작성, 수정, 삭제
  - 조회수 자동 증가
  - 공지글 기능
- **댓글 시스템**
  - 댓글 작성, 삭제
  - 댓글 작성자/관리자만 삭제 가능
- **추천/비추천 시스템**
  - 사용자별 추천/비추천 (중복 방지)
  - 추천/비추천 취소 및 변경 가능
  - 실시간 추천/비추천 수 업데이트
- **신고 시스템**
  - 게시글, 댓글, 사용자 신고
  - 신고 사유: 스팸, 부적절한 내용, 괴롭힘, 저작권 침해, 기타
  - 신고 상태: 대기중, 승인, 거부, 처리완료

### 📁 파일 업로드
- **Cloudinary 연동**
  - 이미지 파일 업로드
  - 썸네일, 이력서 등 파일 관리
  - 환경변수 기반 설정

### 👑 관리자 기능
- **대시보드**
  - 전체 통계: 사용자 수, 게시글 수, 댓글 수, 스터디 수, 신고 수
  - 차단된 사용자 수
  - 최근 신고 내역
- **사용자 관리**
  - 전체 사용자 목록 조회
  - 사용자 차단/차단해제
  - 관리자 계정 보호 (차단 불가)
- **콘텐츠 관리**
  - 게시글 삭제
  - 댓글 삭제
  - 신고 처리

---

## 🛠 기술 세부

### 백엔드 기술
- **Spring Boot 3.5**: 메인 프레임워크
- **Spring Security 6.2**: 인증/인가, 폼 로그인
- **Spring Data JPA**: ORM 및 데이터 접근
- **Hibernate**: JPA 구현체
- **BCrypt**: 비밀번호 암호화
- **Thymeleaf**: 서버사이드 템플릿 엔진

### 데이터베이스
- **개발/테스트**: H2 (인메모리)
- **운영**: MySQL 8.0
- **JPA 설정**: 
  - 개발: `create-drop` (테이블 재생성)
  - 운영: `update` (스키마 자동 업데이트)

### 보안
- **Spring Security**: URL별 접근 권한 설정
- **CSRF**: 개발환경에서 비활성화 (운영환경에서 활성화 필요)
- **세션 기반 인증**: 
- **H2 콘솔**: 개발환경에서 접근 허용

### 클라우드 서비스
- **Cloudinary**: 이미지 업로드 및 관리
- **환경변수**: API 키, 클라우드명, 시크릿 관리


---


## 📊 현재 구현 상태

### ✅ 완료된 기능
- [x] 사용자 인증 시스템 (회원가입/로그인/로그아웃)
- [x] 스터디 CRUD 및 신청/승인 시스템
- [x] 커뮤니티 게시판 (게시글/댓글/추천)
- [x] 신고 시스템
- [x] 관리자 대시보드 및 사용자 관리
- [x] 이미지 업로드 (Cloudinary)
- [x] Spring Security 설정
- [x] 통합 테스트 및 단위 테스트
- [x] 페이징 처리
- [x] 검색 기능
- [x] AWS EC2 배포


### 🔄 진행 중인 작업
- [ ] API 문서화 (Swagger/OpenAPI)
- [ ] 코드 품질 개선 및 중복 제거
- [ ] Javadoc 및 한글 주석 추가

### 📋 향후 계획
- [ ] CI/CD 파이프라인 구축
- [ ] REST API 확장

---

## 👤 개발자
- **DLST316** ([GitHub](https://github.com/DLST316))

---

## 📄 라이선스
이 프로젝트는 개인 포트폴리오 목적으로 제작되었습니다.
