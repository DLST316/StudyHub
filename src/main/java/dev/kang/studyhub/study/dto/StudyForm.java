package dev.kang.studyhub.study.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 스터디 생성/수정 폼 데이터를 담는 DTO 클래스
 * 
 * 주요 필드:
 * - title: 스터디 제목
 * - description: 스터디 설명
 * - recruitmentLimit: 모집 인원 제한
 * - requirement: 모집 조건
 * - deadline: 모집 마감일
 */
@Data
public class StudyForm {
    
    private String title;
    private String description;
    private Integer recruitmentLimit;
    private String requirement;
    private LocalDate deadline;
} 