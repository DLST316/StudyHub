package dev.kang.studyhub.domain.user.model;

/**
 * 사용자의 학력 상태를 정의하는 enum
 */
public enum EducationStatus {
    HIGH_SCHOOL("고등학교 졸업"),      // 고졸
    ENROLLED("재학중"),              // 재학
    GRADUATED("졸업"),               // 졸업
    DROPPED_OUT("중퇴"),            // 중퇴
    LEAVE_OF_ABSENCE("휴학");        // 휴학

    private final String displayName;

    EducationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
