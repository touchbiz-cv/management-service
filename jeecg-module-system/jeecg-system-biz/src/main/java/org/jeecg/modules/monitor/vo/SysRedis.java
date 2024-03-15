package org.jeecg.modules.monitor.vo;

import org.hibernate.validator.constraints.Length;
import org.springframework.data.redis.connection.DataType;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SysRedis implements Comparable<SysRedis> {

    private static final long serialVersionUID = 1L;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id;
    //公共字段
    private String dataType;            // 数据类型
    private String oldRedisKey;         // 原始的缓存key
    private String redisKey;            // 缓存键
    private List<String> redisKeys;            // 缓存键
    private String redisValue;          // 缓存值
    private String expire;              // 过期时间

    //List数据类型
    private String fromLeft;            // 是否从左边添加,仅list类型有效:1左,0右
    private String currentIndex;        // 当前操作的元素的索引，仅list类型有效
    private List<Object> valList;       // list类型的value

    //set数据类型
    private Set<Object> valSet;         // set类型的value

    //zset数据类型
    private List<ScoreVal> zsetList;    // zset类型的value
    private String score;               // 分值,仅zset类型有效

    //hash数据类型
    private String hashKey;             // 仅hash类型有效
    private Map<Object, Object> valMap; // map类型的value

    private Long elCount;             // 集合元素总数

    public static class ScoreVal {
        private String score;
        private String value;

        public ScoreVal() {
        }

        public ScoreVal(String score, String value) {
            this.score = score;
            this.value = value;
        }

        public String getScore() {
            return score;
        }

        public void setScore(String score) {
            this.score = score;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public SysRedis() {
        super();
    }

    public SysRedis(String redisKey, String redisValue) {
        // 默认string类型
        this.dataType = DataType.STRING.code();
        this.redisKey = redisKey;
        this.redisValue = redisValue;
    }

    public SysRedis(String dataType, String redisKey, String redisValue, String expire) {
        this.dataType = dataType;
        this.redisKey = redisKey;
        this.redisValue = redisValue;
        this.expire = expire;
    }

    @NotBlank(message = "数据类型不能为空")
    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getOldRedisKey() {
        return oldRedisKey;
    }

    public void setOldRedisKey(String oldRedisKey) {
        this.oldRedisKey = oldRedisKey;
    }

    @NotBlank(message = "缓存key不能为空")
    @Length(min = 1, max = 200, message = "缓存key长度必须介于 1 和 200 之间")
    public String getRedisKey() {
        return redisKey;
    }

    public void setRedisKey(String redisKey) {
        this.redisKey = redisKey;
    }

    public List<String> getRedisKeys() {
        return redisKeys;
    }

    public void setRedisKeys(List<String> redisKeys) {
        this.redisKeys = redisKeys;
    }


    public String getHashKey() {
        return hashKey;
    }

    public void setHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    public String getFromLeft() {
        return fromLeft;
    }

    public void setFromLeft(String fromLeft) {
        this.fromLeft = fromLeft;
    }

    public String getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(String currentIndex) {
        this.currentIndex = currentIndex;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getRedisValue() {
        return redisValue;
    }

    public void setRedisValue(String redisValue) {
        this.redisValue = redisValue;
    }

    public String getExpire() {
        return expire;
    }

    public void setExpire(String expire) {
        this.expire = expire;
    }

    public List<Object> getValList() {
        return valList;
    }

    public void setValList(List<Object> valList) {
        this.valList = valList;
    }

    public Set<Object> getValSet() {
        return valSet;
    }

    public void setValSet(Set<Object> valSet) {
        this.valSet = valSet;
    }

    public Map<Object, Object> getValMap() {
        return valMap;
    }

    public void setValMap(Map<Object, Object> valMap) {
        this.valMap = valMap;
    }

    public List<ScoreVal> getZsetList() {
        return zsetList;
    }

    public void setZsetList(List<ScoreVal> zsetList) {
        this.zsetList = zsetList;
    }

    public Long getElCount() {
        return elCount;
    }

    public void setElCount(Long elCount) {
        this.elCount = elCount;
    }

    /**
     * 按key排序
     */
    @Override
    public int compareTo(SysRedis sysRedis) {
        return this.redisKey.compareTo(sysRedis.getRedisKey());
    }

}
