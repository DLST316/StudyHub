![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen)
![MySQL](https://img.shields.io/badge/DB-MySQL-yellow)
![Thymeleaf](https://img.shields.io/badge/View-Thymeleaf-lightgrey)

# 🧠 StudyHub (스터디/취준생 커뮤니티)

스터디 모집과 취업 준비생을 위한 커뮤니티 플랫폼입니다.  
Spring Boot 기반 MVC 패턴으로 제작되었으며, 회원 관리, 스터디 기능, 게시판, 관리자 기능까지 포함합니다.

---

## 🚀 프로젝트 개요

| 항목    | 내용                                                                       |
|-------|--------------------------------------------------------------------------|
| 프로젝트명 | StudyHub                                                                 |
| 목적    | 스터디 모집 및 취준생 커뮤니티 서비스                                                    |
| 대상    | 백엔드 개발 취업용 포트폴리오                                                         |
| 기술 스택 | Java 17, Spring Boot 3.5, Spring Security, JPA, MySQL, Thymeleaf, Gradle |
| 배포 환경 | AWS EC2 (Docker)                                                         |

---

## 🧩 주요 기능

### 🔐 회원
- 회원가입 / 로그인 / 로그아웃
- 학력(고졸 + 재학/졸업/중퇴 + 학교명/전공) 등록
- 비밀번호 암호화 (BCrypt)

### 📚 스터디
- 스터디 개설 / 수정 / 삭제
- 모집 인원/조건 지정 및 신청/승인 관리
- 신청자 관리, 승인 기능 포함

### 📝 게시판
- 자유게시판, QnA, 후기, 면접후기 등 카테고리별 글 작성
- 댓글 / 대댓글 기능
- 좋아요 / 신고 기능

### 📁 파일 업로드
- 썸네일, 이력서 파일 업로드
- 파일 메타데이터 관리 (확장 예정)

### 👑 관리자
- 유저 강퇴, 글 삭제, 신고 처리
- 관리자 전용 로그인

---

## 🛠 기술 세부

- Spring MVC 구조 기반 서버 렌더링
- Spring Data JPA로 ORM 처리
- 로그인: 세션 방식 or JWT (확장 예정)
- Thymeleaf 템플릿 엔진
- Swagger 도입 (REST API 기능 확장 시)
- 로컬 개발용 H2 / 운영 MySQL 연동


---

## 🔗 배포 주소 (예정)
- [https://studyhub.dev](https://studyhub.dev) (추후 연결)

## 👤 개발자
- DLST316 ([GitHub](https://github.com/DLST316))

---
