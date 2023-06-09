package com._8attery.seesaw.dto.api.request;

import com._8attery.seesaw.domain.project_emotion.Emotion;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectEmotionRequestDto {

    @NotNull
    @Positive
    private Long projectId;

    @NotNull
    private Emotion emotion;
}
