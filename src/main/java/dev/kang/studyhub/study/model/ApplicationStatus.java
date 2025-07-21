package dev.kang.studyhub.study.model;

/**
 * 스터디 신청 상태를 정의하는 enum
 * 
 * PENDING: 대기중 (기본값)
 * APPROVED: 승인됨
 * REJECTED: 거절됨
 */
public enum ApplicationStatus {
    PENDING,    // 대기중
    APPROVED,   // 승인됨
    REJECTED    // 거절됨
} 