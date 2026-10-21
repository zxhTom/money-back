package cn.iocoder.yudao.module.custom.service.milestone;

import cn.iocoder.yudao.module.custom.controller.admin.milestone.vo.*;
import cn.iocoder.yudao.module.custom.dal.dataobject.milestone.MilestoneDO;
import cn.iocoder.yudao.module.custom.dal.mysql.milestone.MilestoneMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MilestoneServiceTest {
    @Mock MilestoneMapper mapper;
    @InjectMocks MilestoneServiceImpl service;

    @Test void missingDetailIsNull() {
        assertNull(MilestoneServiceImpl.convertToResp(null, LocalDate.now()));
    }

    @Test void createsWithDefaults() {
        MilestoneCreateReqVO req = new MilestoneCreateReqVO();
        req.setTitle("证书续期"); req.setCategory("DOMAIN"); req.setTargetDate(LocalDate.now());
        service.createMilestone(req);
        ArgumentCaptor<MilestoneDO> captor = ArgumentCaptor.forClass(MilestoneDO.class);
        verify(mapper).insert(captor.capture());
        assertEquals(0, captor.getValue().getStatus());
        assertEquals(30, captor.getValue().getRemindDays());
        assertEquals("MEDIUM", captor.getValue().getPriority());
    }

    @Test void warningSummaryUsesConfiguredWindowAndHandlesLegacyNulls() {
        MilestoneDO due = MilestoneDO.builder().targetDate(LocalDate.now().plusDays(20)).remindDays(30).build();
        MilestoneDO normal = MilestoneDO.builder().targetDate(LocalDate.now().plusDays(10)).remindDays(5).status(0).build();
        MilestoneDO complete = MilestoneDO.builder().targetDate(LocalDate.now().minusDays(1)).status(2).build();
        MilestoneDO unknown = MilestoneDO.builder().remindDays(3650).build();
        when(mapper.selectList()).thenReturn(Arrays.asList(due, normal, complete, unknown));
        MilestoneSummaryRespVO summary = service.getMilestoneSummary();
        assertEquals(1, summary.getUrgentCount());
        assertEquals(0, summary.getExpiredCount());
        assertEquals(1, summary.getCompletedCount());
        assertEquals(3, summary.getPendingCount());
        assertEquals(1, summary.getUpcomingList().size());
        assertEquals("URGENT", summary.getUpcomingList().get(0).getAlertStatus());
    }

    @Test void missingRecordDoesNotWrite() {
        MilestoneUpdateReqVO req = new MilestoneUpdateReqVO(); req.setId(42L);
        assertThrows(RuntimeException.class, () -> service.updateMilestone(req));
        verify(mapper, never()).updateById(any(MilestoneDO.class));
    }

    @Test void datesAreIsoStringsRegardlessOfMapperTimestampDefaults() throws Exception {
        MilestoneRespVO resp = new MilestoneRespVO(); resp.setTargetDate(LocalDate.of(2026, 10, 21));
        String json = new ObjectMapper().registerModule(new JavaTimeModule()).writeValueAsString(resp);
        assertTrue(json.contains("\"targetDate\":\"2026-10-21\""));
    }

    @Test void rejectsMissingAndInvalidInput() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            MilestoneCreateReqVO req = new MilestoneCreateReqVO();
            assertEquals(3, factory.getValidator().validate(req).size());
            req.setTitle("有效标题"); req.setCategory("DOMAIN"); req.setTargetDate(LocalDate.now());
            assertTrue(factory.getValidator().validate(req).isEmpty());
            req.setRemindDays(-1); req.setStatus(4); req.setPriority("INVALID");
            assertEquals(3, factory.getValidator().validate(req).size());
        }
    }
}
