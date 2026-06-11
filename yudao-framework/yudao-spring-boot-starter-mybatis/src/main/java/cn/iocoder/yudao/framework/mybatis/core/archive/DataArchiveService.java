package cn.iocoder.yudao.framework.mybatis.core.archive;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据归档通用 Service
 * <p>
 * 将主库中满足条件的历史数据迁移到 archive 归档库，流程：
 * 1. 从主库分批查询满足归档条件的记录
 * 2. 批量写入 archive 库对应表
 * 3. 从主库物理删除已归档记录（写入成功后才删）
 */
@Slf4j
public class DataArchiveService {

    /**
     * 归档一批数据（查询主库 → 写入归档库 → 删除主库）
     *
     * @param mainMapper    主库 Mapper
     * @param archiveMapper 归档库 Mapper（需标注 @DS("archive")）
     * @param condition     归档条件
     * @param batchSize     单批处理条数
     * @param <T>           数据对象类型
     * @return 本次归档条数
     */
    public <T> int archiveBatch(BaseMapperX<T> mainMapper,
                                BaseMapperX<T> archiveMapper,
                                LambdaQueryWrapper<T> condition,
                                int batchSize) {
        condition.last("LIMIT " + batchSize);
        List<T> records = mainMapper.selectList(condition);
        if (records.isEmpty()) {
            return 0;
        }

        archiveMapper.insertBatch(records);

        List<Long> ids = records.stream()
                .map(this::extractId)
                .collect(Collectors.toList());
        mainMapper.deleteBatchIds(ids);

        log.info("[archiveBatch][归档 {} 条记录]", records.size());
        return records.size();
    }

    /**
     * 将已归档记录同步回主库
     *
     * @param id            记录 ID
     * @param mainMapper    主库 Mapper
     * @param archiveMapper 归档库 Mapper
     * @param <T>           数据对象类型
     * @return 恢复的记录，不存在时返回 null
     */
    public <T> T restoreFromArchive(Long id,
                                    BaseMapperX<T> mainMapper,
                                    BaseMapperX<T> archiveMapper) {
        T archived = archiveMapper.selectById(id);
        if (archived == null) {
            return null;
        }
        mainMapper.insert(archived);
        archiveMapper.deleteById(id);
        log.info("[restoreFromArchive][从归档库恢复记录 id={}]", id);
        return archived;
    }

    /**
     * 查询单条记录：主库优先，主库不存在时从归档库查找
     *
     * @param id            记录 ID
     * @param mainMapper    主库 Mapper
     * @param archiveMapper 归档库 Mapper
     * @param <T>           数据对象类型
     * @return 记录，两者均不存在时返回 null
     */
    public <T> T getWithArchiveFallback(Long id,
                                        BaseMapperX<T> mainMapper,
                                        BaseMapperX<T> archiveMapper) {
        T record = mainMapper.selectById(id);
        if (record != null) {
            return record;
        }
        return archiveMapper.selectById(id);
    }

    /**
     * 获取归档截止时间（N 个月前）
     */
    public static LocalDateTime archiveThreshold(int months) {
        return LocalDateTime.now().minusMonths(months);
    }

    @SuppressWarnings("unchecked")
    private <T> Long extractId(T record) {
        try {
            Field idField = record.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            return (Long) idField.get(record);
        } catch (Exception e) {
            throw new IllegalStateException("归档对象缺少 id 字段: " + record.getClass().getName(), e);
        }
    }

}
