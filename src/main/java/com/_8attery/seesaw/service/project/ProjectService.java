package com._8attery.seesaw.service.project;

import com._8attery.seesaw.domain.battery.Battery;
import com._8attery.seesaw.domain.battery.BatteryRepositoryCustom;
import com._8attery.seesaw.domain.charge.ChargeRepositoryCustom;
import com._8attery.seesaw.domain.project.Intensity;
import com._8attery.seesaw.domain.project.Project;
import com._8attery.seesaw.domain.project.ProjectRepository;
import com._8attery.seesaw.domain.project.ProjectRepositoryCustom;
import com._8attery.seesaw.domain.project_emotion.Emotion;
import com._8attery.seesaw.domain.project_emotion.ProjectEmotion;
import com._8attery.seesaw.domain.project_emotion.ProjectEmotionRepository;
import com._8attery.seesaw.domain.project_emotion.ProjectEmotionRepositoryCustom;
import com._8attery.seesaw.domain.project_qna.ProjectQna;
import com._8attery.seesaw.domain.project_qna.ProjectQnaRepository;
import com._8attery.seesaw.domain.project_qna.ProjectQnaRepositoryCustom;
import com._8attery.seesaw.domain.project_question.ProjectQuestion;
import com._8attery.seesaw.domain.project_question.ProjectQuestionRepository;
import com._8attery.seesaw.domain.project_question.ProjectQuestionRepositoryCustom;
import com._8attery.seesaw.domain.project_question.QuestionType;
import com._8attery.seesaw.domain.project_record.ProjectRecord;
import com._8attery.seesaw.domain.project_record.ProjectRecordRepository;
import com._8attery.seesaw.domain.project_record.ProjectRecordRepositoryCustom;
import com._8attery.seesaw.domain.project_remembrance.ProjectRemembrance;
import com._8attery.seesaw.domain.project_remembrance.ProjectRemembranceRepository;
import com._8attery.seesaw.domain.project_remembrance.ProjectRemembranceRepositoryCustom;
import com._8attery.seesaw.domain.project_remembrance.RemembranceType;
import com._8attery.seesaw.domain.user.User;
import com._8attery.seesaw.domain.value.Value;
import com._8attery.seesaw.domain.value.ValueRepositoryCustom;
import com._8attery.seesaw.dto.api.request.ProjectEmotionRequestDto;
import com._8attery.seesaw.dto.api.request.ProjectQnaRequestDto;
import com._8attery.seesaw.dto.api.request.ProjectRecordRequestDto;
import com._8attery.seesaw.dto.api.request.ProjectRemembranceRequestDto;
import com._8attery.seesaw.dto.api.response.*;
import com._8attery.seesaw.exception.BaseException;
import com._8attery.seesaw.exception.custom.ConflictRequestException;
import com._8attery.seesaw.exception.custom.ResourceNotFoundException;
import com._8attery.seesaw.service.util.ServiceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com._8attery.seesaw.exception.BaseResponseStatus.DATABASE_ERROR;
import static com._8attery.seesaw.exception.BaseResponseStatus.POSTS_EMPTY_POST_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {
    private static final long[] REPORT_FLOW = new long[]{77L, 83L, 78L, 84L, 80L, 88L, 81L, 89L, 82L, 90L};

    public static final Map<Emotion, String> emotionStringMap = new HashMap<>(
            Map.of(
                    Emotion.LIKE, "행복",
                    Emotion.NICE, "뿌듯함",
                    Emotion.IDK, "아쉬움",
                    Emotion.ANGRY, "힘듦",
                    Emotion.SAD, "슬픔"
            )
    );

    private final ProjectQuestionRepository projectQuestionRepository;
    private final ProjectRepository projectRepository;
    private final ProjectRepositoryCustom projectRepositoryCustom;
    private final ProjectEmotionRepository projectEmotionRepository;
    private final ProjectEmotionRepositoryCustom projectEmotionRepositoryCustom;
    private final ProjectRecordRepository projectRecordRepository;
    private final ProjectRecordRepositoryCustom projectRecordRepositoryCustom;
    private final ProjectRemembranceRepository projectRemembranceRepository;
    private final ProjectRemembranceRepositoryCustom projectRemembranceRepositoryCustom;
    private final ProjectQnaRepository projectQnaRepository;
    private final ProjectQnaRepositoryCustom projectQnaRepositoryCustom;
    private final ProjectQuestionRepositoryCustom projectQuestionRepositoryCustom;
    private final ValueRepositoryCustom valueRepositoryCustom;
    private final BatteryRepositoryCustom batteryRepositoryCustom;
    private final ServiceUtils serviceUtils;
    private final ChargeRepositoryCustom chargeRepositoryCustom;
    private static final int MIN = 46;
    private static final int MAX = 76;

    @Transactional
    public ProjectResponseDto addUserProject(Long userId, Long valueId, String projectName, LocalDateTime startedAt, LocalDateTime endedAt, String intensity, String goal) throws BaseException {
        User retrievedUser = serviceUtils.retrieveUserById(userId);
        Value retrievedValue = serviceUtils.retrieveValueById(valueId);
//        try {
//            projectRepository.addIngProject(userId, valueId, projectName, startedAt, endedAt, intensity, goal);
//
//            Optional<ProjectResponseDto> projectResponseDto = projectRepository.getProject(userId, valueId, projectName);
//            return projectResponseDto.orElse(new ProjectResponseDto(0L, null, null, null, null, null, false));
//
//        } catch (Exception exception) {
//            log.info(exception.getMessage());exception.printStackTrace();
//            throw new BaseException(DATABASE_ERROR);
//        }

        Project newProject =
                Project.builder()
                        .user(retrievedUser)
                        .value(retrievedValue)
                        .projectName(projectName)
                        .startedAt(startedAt)
                        .endedAt(endedAt)
                        .intensity(Intensity.valueOf(intensity))
                        .goal(goal)
                        .isFinished(false)
                        .build();

        projectRepository.save(newProject);

        projectEmotionRepository.save(
                ProjectEmotion.builder()
                        .project(newProject)
                        .likeCnt(0)
                        .niceCnt(0)
                        .idkCnt(0)
                        .sadCnt(0)
                        .angryCnt(0)
                        .build()
        );

        return ProjectResponseDto.of(newProject);
    }

    @Transactional
    public ProjectResponseDto updateUserProject(Long projectId, Long userId, Long valueId, String projectName, LocalDateTime startedAt, LocalDateTime endedAt, String intensity, String goal) throws BaseException {
        try {
            projectRepository.updateIngProject(projectId, valueId, projectName, startedAt, endedAt, intensity, goal);

            Optional<ProjectResponseDto> projectResponseDto = projectRepository.getProject(userId, valueId, projectName);
            return projectResponseDto.orElse(new ProjectResponseDto(0L, null, null, null, null, null, false));

        } catch (Exception exception) {
            log.info(exception.getMessage());
            exception.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public Long retrieveUserId(Long projectId) throws BaseException {
        try {
            Long userId = projectRepository.getUserId(projectId);
            if (userId == null)
                throw new BaseException(POSTS_EMPTY_POST_ID);
            return userId;
        } catch (Exception exception) {
            log.info(exception.getMessage());
            exception.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    @Transactional
    public void deleteUserProject(Long projectId) throws BaseException {
//        try {
//            int result = projectRepository.deleteProjectByProjectId(projectId);
//            projectRepository.d
//            if (result == 0)
//                throw new BaseException(DELETE_FAIL_POST);
//        } catch (Exception exception) {
//            log.info(exception.getMessage());exception.printStackTrace();
//            throw new BaseException(DATABASE_ERROR);
//        }
        Project retrievedProject = serviceUtils.retrieveProjectById(projectId);
        projectRepository.delete(retrievedProject);
    }

    public List<ProjectCardResponseDto> getProgressProjectList(Long userId) throws BaseException {
        try {
            List<ProjectCardResponseDto> list = projectRepository.findProgressProjectList(userId);
            return list;
        } catch (Exception exception) {
            log.info(exception.getMessage());
            exception.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public List<ProjectCardResponseDto> getCompleteProjectList(Long userId) throws BaseException {
        try {
            List<ProjectCardResponseDto> list = projectRepository.findCompleteProjectList(userId);
            return list;
        } catch (Exception exception) {
            log.info(exception.getMessage());
            exception.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    // 진행 중, 완료된 프로젝트 카운팅 조회
    public ProjectCountResponseDto getUserProjectCount(Long userId) throws BaseException {
        try {
            Integer progressCount = projectRepository.getProgressCount(userId);
            Integer completeCount = projectRepository.getCompleteCount(userId);

            ProjectCountResponseDto res = new ProjectCountResponseDto(progressCount, completeCount);
            return res;
        } catch (Exception exception) {
            log.info(exception.getMessage());
            exception.printStackTrace();
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public ProjectDetailsResponseDto getProjectDetails(Long userId, Long projectId) {
        serviceUtils.retrieveUserById(userId);
        Project rerievedProject = serviceUtils.retrieveProjectById(projectId);
        ProjectDetailsResponseDto result = projectRepositoryCustom.getProjectDetails(projectId);

        if (!result.getIsFinished()) {
            LocalDate middleDate = rerievedProject.getStartedAt().plusDays(ChronoUnit.DAYS.between(rerievedProject.getStartedAt(), rerievedProject.getEndedAt()) / 2).toLocalDate();
            result.setIsHalfProgressed(LocalDateTime.now().toLocalDate().isAfter(middleDate.minusDays(1L)));
        }

        if (result.getMiddleRemembranceId() != null && result.getFinalRemembranceId() != null) {
            result.setIsProjectReport(true);
            List<String> answerContentList = projectQnaRepositoryCustom.findProjectQnaContentsListByProjectRemembranceId(result.getMiddleRemembranceId());
            answerContentList.addAll(projectQnaRepositoryCustom.findProjectQnaContentsListByProjectRemembranceId(result.getFinalRemembranceId()));
            for (String s : answerContentList) {
                if (s == null) {
                    result.setIsProjectReport(false);
                    break;
                }
            }
        }

        return result;
    }

    @Transactional
    public ProjectEmotionResponseDto addEmotionToProject(Long userId, ProjectEmotionRequestDto projectEmotionRequestDto) {
        serviceUtils.retrieveUserById(userId);
        serviceUtils.retrieveProjectById(projectEmotionRequestDto.getProjectId());

        return projectEmotionRepositoryCustom.updateProjectEmotion(projectEmotionRequestDto);
    }

    @Transactional
    public ProjectRecordResponseDto addRecordToProject(Long userId, ProjectRecordRequestDto projectRecordRequestDto) {
        serviceUtils.retrieveUserById(userId);
        Project retrievedProject = serviceUtils.retrieveProjectById(projectRecordRequestDto.getProjectId());
        ProjectQuestion retrievedQuestion = null;
        if (projectRecordRequestDto.getProjectQuestionId() != null) {
            retrievedQuestion = serviceUtils.retrieveProjectQuestionById(projectRecordRequestDto.getProjectQuestionId());
        } else {
            retrievedQuestion = serviceUtils.retrieveProjectQuestionById(1L);
        }

        return ProjectRecordResponseDto.from(projectRecordRepository.save(
                        ProjectRecord.builder()
                                .project(retrievedProject)
                                .createdAt(LocalDateTime.now())
                                .projectQuestion(retrievedQuestion)
                                .temp(projectRecordRequestDto.getTemp())
                                .contents(projectRecordRequestDto.getContents())
                                .build()
                )
        );
    }

    public List<ProjectRecordResponseDto> getProjectRecordList(Long userId, Long projectId) {
        serviceUtils.retrieveUserById(userId);
        serviceUtils.retrieveProjectById(projectId);

        return projectRecordRepositoryCustom.findAllRecordsByProjectId(projectId);
    }

    public ProjectQuestionResponseDto getRandomRegularQuestion(Long userId) {
        serviceUtils.retrieveUserById(userId);
        ProjectQuestion question = serviceUtils.retrieveProjectQuestionById(getTodayQuestionId());

        return ProjectQuestionResponseDto
                .builder()
                .id(question.getId())
                .contents(question.getContents())
                .build();
    }

    @Transactional
    public ProjectRemembranceResponseDto addRemembranceToProject(Long userId, ProjectRemembranceRequestDto projectRemembranceRequestDto) {
        User retrievedUser = serviceUtils.retrieveUserById(userId);
        Project retrievedProject = serviceUtils.retrieveProjectById(projectRemembranceRequestDto.getProjectId());

        if (projectRemembranceRepositoryCustom.existsByProjectIdAndType(projectRemembranceRequestDto.getProjectId(), projectRemembranceRequestDto.getType())) {
            throw new ConflictRequestException("이미 해당 타입의 회고록이 존재합니다.");
        }

        ProjectEmotion retrievedProjectEmotion = serviceUtils.retrieveProjectEmotionByProjectId(projectRemembranceRequestDto.getProjectId());

        Map<Emotion, Integer> emotionMap = new HashMap<>();
        emotionMap.put(Emotion.LIKE, retrievedProjectEmotion.getLikeCnt());
        emotionMap.put(Emotion.NICE, retrievedProjectEmotion.getNiceCnt());
        emotionMap.put(Emotion.IDK, retrievedProjectEmotion.getIdkCnt());
        emotionMap.put(Emotion.ANGRY, retrievedProjectEmotion.getAngryCnt());
        emotionMap.put(Emotion.SAD, retrievedProjectEmotion.getSadCnt());


        ProjectRemembrance projectRemembrance = projectRemembranceRepository.save(
                ProjectRemembrance.builder()
                        .project(retrievedProject)
                        .date(LocalDateTime.now())
                        .type(projectRemembranceRequestDto.getType())
                        .emotion(Collections.max(emotionMap.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey())
                        .project(retrievedProject)
                        .build()
        ); // 새로운 프로젝트 회고록 생성

        List<ProjectQuestion> projectQuestionList = projectQuestionRepositoryCustom.findAllByQuestionType(QuestionType.valueOf(projectRemembranceRequestDto.getType().toString()));
        // 회고 타입에 맞는 질문 리스트 가져오기

        projectQuestionList.forEach(
                projectQuestion -> projectQnaRepository.save(ProjectQna
                        .builder()
                        .projectRemembrance(projectRemembrance)
                        .projectQuestion(projectQuestion)
                        .build())); // 해당 프로젝트에 회고용 질문 추가

        return ProjectRemembranceResponseDto
                .builder()
                .remembranceId(projectRemembrance.getId())
                .qnaList(projectRemembranceRepositoryCustom.findQnaListByRemembranceId(projectRemembrance.getId()))
                .remembranceType(projectRemembrance.getType())
                .userName(retrievedUser.getNickName())
                .emotion(emotionStringMap.get(projectRemembrance.getEmotion()))
                .build();
    }

    public ProjectRemembranceResponseDto getProjectRemembrance(Long userId, Long remembranceId) {
        User retrievedUser = serviceUtils.retrieveUserById(userId);
        ProjectRemembrance retrievedRemembrance = serviceUtils.retrieveProjectRemembranceById(remembranceId);

        return ProjectRemembranceResponseDto
                .builder()
                .remembranceId(retrievedRemembrance.getId())
                .qnaList(projectRemembranceRepositoryCustom.findQnaListByRemembranceId(remembranceId))
                .userName(retrievedUser.getNickName())
                .emotion(emotionStringMap.get(retrievedRemembrance.getEmotion()))
                .remembranceType(retrievedRemembrance.getType())
                .build();
    }

    @Transactional
    public ProjectQnaResponseDto addAnswerToProjectQna(Long userId, ProjectQnaRequestDto projectQnaRequestDto) {
        serviceUtils.retrieveUserById(userId);
        ProjectQna retrievedProjectQna = serviceUtils.retrieveProjectQnaById(projectQnaRequestDto.getProjectQnaId());
        retrievedProjectQna.updateAnswer(projectQnaRequestDto.getAnswerChoice(), projectQnaRequestDto.getAnswerContent());

        projectQnaRepository.save(retrievedProjectQna);

        return projectQnaRepositoryCustom.findProjectQnaByProjectQnaId(retrievedProjectQna.getId());
    }

    public ProjectQnaResponseDto getProjectQna(Long userId, Long qnaId) {
        serviceUtils.retrieveUserById(userId);

        return projectQnaRepositoryCustom.findProjectQnaByProjectQnaId(serviceUtils.retrieveProjectQnaById(qnaId).getId());
    }

    @Transactional
    public List<Long> deleteProjectRecordList(Long userId, List<Long> projectRecordIdList) {
        serviceUtils.retrieveUserById(userId);

        List<ProjectRecord> projectRecordList = projectRecordRepositoryCustom.findAllByProjectRecordIdList(userId, projectRecordIdList);

        if (projectRecordList.size() == 0) {
            throw new ResourceNotFoundException("존재하지 않는 상시 회고입니다.");
        }

        List<Long> resultList = projectRecordList.stream().map(ProjectRecord::getId).collect(Collectors.toList());

        projectRecordRepository.deleteAllInBatch(projectRecordList);

        return resultList;
    }

    public InitialProjectReportResponseDto getInitialProjectReport(Long userId, Long projectId) {
        serviceUtils.retrieveUserById(userId);
        Project retrievedProject = serviceUtils.retrieveProjectById(projectId);
        if (!projectRemembranceRepositoryCustom.existsByProjectIdAndType(projectId, RemembranceType.MIDDLE) || !projectRemembranceRepositoryCustom.existsByProjectIdAndType(projectId, RemembranceType.FINAL)) {
            throw new ConflictRequestException("중간 혹은 최종 회고록이 존재하지 않습니다.");
        }
        ProjectReportInfoDto projectReportInfoDto = projectRepositoryCustom.getProjectReportInfo(projectId);
        projectReportInfoDto.setJointProject(projectRepositoryCustom.getJointProject(userId, projectId, retrievedProject.getStartedAt(), retrievedProject.getEndedAt()));
        ProjectRemembrance middleRemembrance = projectRemembranceRepositoryCustom.getByProjectIdAndType(projectId, RemembranceType.MIDDLE);
        ProjectRemembrance finalRemembrance = projectRemembranceRepositoryCustom.getByProjectIdAndType(projectId, RemembranceType.FINAL);

        List<ProjectQna> projectQnaList = new ArrayList<>();

//        projectQnaList.add(projectQnaRepositoryCustom.findProjectQnaByProjectRemembranceAndQuestionId(middleRemembrance.getId(), 77L));
//        projectQnaList.add(projectQnaRepositoryCustom.findProjectQnaByProjectRemembranceAndQuestionId(finalRemembrance.getId(), 83L));
//        projectQnaList.add(projectQnaRepositoryCustom.findProjectQnaByProjectRemembranceAndQuestionId(middleRemembrance.getId(), 78L));
//        projectQnaList.add(projectQnaRepositoryCustom.findProjectQnaByProjectRemembranceAndQuestionId(finalRemembrance.getId(), 84L));
//        projectQnaList.add(projectQnaRepositoryCustom.findProjectQnaByProjectRemembranceAndQuestionId(middleRemembrance.getId(), 80L));
//        projectQnaList.add(projectQnaRepositoryCustom.findProjectQnaByProjectRemembranceAndQuestionId(finalRemembrance.getId(), 88L));
//        projectQnaList.add(projectQnaRepositoryCustom.findProjectQnaByProjectRemembranceAndQuestionId(middleRemembrance.getId(), 81L));
//        projectQnaList.add(projectQnaRepositoryCustom.findProjectQnaByProjectRemembranceAndQuestionId(finalRemembrance.getId(), 89L));
//        projectQnaList.add(projectQnaRepositoryCustom.findProjectQnaByProjectRemembranceAndQuestionId(middleRemembrance.getId(), 82L));
//        projectQnaList.add(projectQnaRepositoryCustom.findProjectQnaByProjectRemembranceAndQuestionId(finalRemembrance.getId(), 90L));

        for (int i = 0; i < REPORT_FLOW.length; i++) {
            ProjectQna retrievedQna = projectQnaRepositoryCustom.findProjectQnaByProjectRemembranceAndQuestionId(middleRemembrance.getId(), REPORT_FLOW[i]);
            if (retrievedQna == null) {
                log.info("{}번 질문이 존재하지 않음", REPORT_FLOW[i]);
                throw new ConflictRequestException("중간 회고록이 유효하지 않습니다.");
            }
            projectQnaList.add(retrievedQna);
            retrievedQna = projectQnaRepositoryCustom.findProjectQnaByProjectRemembranceAndQuestionId(finalRemembrance.getId(), REPORT_FLOW[++i]);
            if (retrievedQna == null) {
                log.info("{}번 질문이 존재하지 않음", REPORT_FLOW[i]);
                throw new ConflictRequestException("최종 회고록이 유효하지 않습니다.");
            }
            projectQnaList.add(retrievedQna);
        }


        List<InitialProjectReportResponseDto.AnswerContentDto> answerContentDtoList = projectQnaList.stream().map(projectQna -> InitialProjectReportResponseDto.AnswerContentDto
                .builder()
                .choice(projectQna.getAnswerChoice())
                .content(projectQna.getAnswerContent())
                .remembranceType(projectQna.getProjectRemembrance().getType())
                .build()).collect(Collectors.toList());

        InitialProjectReportResponseDto resultDto = InitialProjectReportResponseDto
                .builder()
                .projectReportInfoDto(projectReportInfoDto)
                .emotion1(middleRemembrance.getEmotion())
                .emotion2(finalRemembrance.getEmotion())
                .projectMiddleFinalQnaDto(
                        ProjectMiddleFinalQnaDto
                                .builder()
                                .middleAnswer(projectQnaRepositoryCustom.findProjectQnaByProjectRemembranceAndQuestionId(middleRemembrance.getId(), 79L).getAnswerContent())
                                .finalAnswer(projectQnaRepositoryCustom.findProjectQnaByProjectRemembranceAndQuestionId(finalRemembrance.getId(), 86L).getAnswerContent())
                                .build()
                )
                .todayAnswerCount(projectRecordRepositoryCustom.countAllByProjectAndQuestionExists(retrievedProject.getId(), true))
                .recordAnswerCount(projectRecordRepositoryCustom.countAllByProjectAndQuestionExists(retrievedProject.getId(), false))
                .simpleRecordInfo(projectRecordRepositoryCustom.findThreeRandomSimpleRecordByProjectId(retrievedProject.getId()))
                .answerContentList(answerContentDtoList)
                .build();

        return resultDto;
    }

    private Long getTodayQuestionId() {
        String formattedDate = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        int hashAsInt = Math.abs(formattedDate.hashCode());
        return (long) MIN + (hashAsInt % (MAX - MIN + 1));
    }

    public ProjectReportBatteryResponseDto getProjectReportBattery(Long userId, Long projectId) {
        User retrievedUser = serviceUtils.retrieveUserById(userId);
        Project retrievedProject = serviceUtils.retrieveProjectById(projectId);
        Battery retrievedBattery = serviceUtils.retrieveBatteryById(retrievedUser.getBattery().getId());

        LocalDateTime startedAt = retrievedProject.getStartedAt();
        LocalDateTime startedAtToLocalDate = startedAt.toLocalDate().withDayOfYear(1).atStartOfDay();
        LocalDateTime startedAtToLocalDatePlusOneYear = startedAtToLocalDate.plusYears(1).minus(1, ChronoUnit.SECONDS);

        LocalDateTime endedAt = retrievedProject.getEndedAt();

        ProjectReportBatteryResponseDto result = batteryRepositoryCustom.getProjectReportBatteryByTerm(retrievedBattery.getId(), startedAt, endedAt);
        List<Value> valueInProjectYearList = valueRepositoryCustom.getValueInProjectYear(retrievedUser.getId(), startedAtToLocalDate, startedAtToLocalDatePlusOneYear);

        List<ProjectReportBatteryResponseDto.ValueNameAndCount> valueNameAndCountList = valueInProjectYearList
                .stream()
                .map(value -> ProjectReportBatteryResponseDto.ValueNameAndCount
                        .builder()
                        .name(value.getValueName())
                        .count(chargeRepositoryCustom.countChargeByValueIdAndTerm(value.getId(), startedAt, endedAt))
                        .build()
                ).collect(Collectors.toList());

        result.setChargeReport(
                ProjectReportBatteryResponseDto.ChargeReport
                        .builder()
                        .valueCountList(valueNameAndCountList)
                        .build()
        );

        return result;
    }

}
