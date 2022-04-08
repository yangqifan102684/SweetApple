//package com.sweet.apple.sweetapple.controller;
//
//import cn.techwolf.boss.bloom.filter.BloomFilter;
//import cn.techwolf.boss.bloom.filter.service.IBloomFilterCache;
//import cn.techwolf.cache.service.ICacheService;
//import com.google.common.collect.Lists;
//import com.google.common.collect.Maps;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Calendar;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.Callable;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.Executors;
//import java.util.concurrent.Future;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
//import javax.annotation.PostConstruct;
//import javax.annotation.Resource;
//import org.apache.commons.collections4.CollectionUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public abstract class AbstractRedisBloomFilterCache implements IBloomFilterCache {
//    private static final int DEFAULT_PART = 16;
//    private boolean open = false;
//    private BloomFilter bloomFilter;
//    private final Logger logger = LoggerFactory.getLogger(this.getClass());
//    private static final ScheduledExecutorService SCHEDULED_THREAD_POOL = Executors.newScheduledThreadPool(1);
//    private ThreadPoolExecutor batchThreadPool;
//    @Resource(
//            name = "bloomCacheService"
//    )
//    private ICacheService bloomCacheService;
//    private static final int MAX_BIT_SIZE = 536870912;
//
//    public AbstractRedisBloomFilterCache() {
//    }
//
//    protected abstract String getBloomFilterRedisKeyPrefix();
//
//    private String getSwitchKey() {
//        return "switch:" + this.getBloomFilterRedisKeyPrefix();
//    }
//
//    public float getErrorRate() {
//        return 0.05F;
//    }
//
//    public int getPartCount() {
//        return 16;
//    }
//
//    @PostConstruct
//    public void init() {
//        try {
//            int maxKey = this.getMaxKey() % this.getPartCount() == 0 ? this.getMaxKey() / this.getPartCount() : (this.getMaxKey() + this.getPartCount()) / this.getPartCount();
//            this.bloomFilter = new BloomFilter(maxKey, this.getErrorRate());
//            if (maxKey * this.bloomFilter.getHashFunctionCount() > 536870912) {
//                this.logger.error("init:{} bloomFilter fail, elements bits must lower than {}!!!", this.getBloomFilterRedisKeyPrefix(), 536870912);
//                return;
//            }
//
//            int partCount = this.getPartCount();
//            this.batchThreadPool = new ThreadPoolExecutor(partCount, partCount, 5L, TimeUnit.MINUTES, new ArrayBlockingQueue(10000), new CallerRunsPolicy());
//            SCHEDULED_THREAD_POOL.scheduleWithFixedDelay(() -> {
//                this.open = "1".equals(this.bloomCacheService.get(this.getSwitchKey()));
//                if (this.open && !this.existKey()) {
//                    this.logger.error("use bloomFilter, but not exist key!bloomFilterKey:{}", this.getBloomFilterRedisKeyPrefix());
//                    boolean result = this.turnOff();
//                    if (!result) {
//                        this.logger.error("turnOff fail!bloomFilterKey:{}", this.getBloomFilterRedisKeyPrefix());
//                    }
//                }
//
//            }, 1L, 1L, TimeUnit.SECONDS);
//        } catch (Exception var3) {
//            this.logger.error("checkBloomFilterKey err", var3);
//        }
//
//    }
//
//    public boolean isOpen() {
//        return this.open;
//    }
//
//    public boolean turnOff() {
//        this.bloomCacheService.set(this.getSwitchKey(), "0");
//        this.open = false;
//
//        try {
//            TimeUnit.SECONDS.sleep(5L);
//        } catch (InterruptedException var2) {
//            Thread.currentThread().interrupt();
//        }
//
//        return true;
//    }
//
//    public boolean turnOn() {
//        this.bloomCacheService.set(this.getSwitchKey(), "1");
//        this.open = true;
//        return true;
//    }
//
//    public void insert(String element, boolean init) {
//        if (element == null) {
//            this.logger.error("insert param is null, ({})", this.getBloomFilterRedisKeyPrefix());
//        } else {
//            if (!this.isOpen() && !init && !this.inSafeTime()) {
//                this.logger.error("insert param, but not open bloom cn.techwolf.boss.bloom.filter, element:{}, ({})", element, this.getBloomFilterRedisKeyPrefix());
//            }
//
//            try {
//                long[] offsets = this.bloomFilter.murmurHashOffset(element);
//                String offsetToString = Arrays.toString(offsets);
//                this.logger.info("insert key:{}, element:{},offsets:{}", new Object[]{this.getBloomFilterRedisKeyPrefix(), element, offsetToString});
//                Map<Long, Boolean> bitsMap = Maps.newHashMapWithExpectedSize(offsets.length);
//                long[] var6 = offsets;
//                int var7 = offsets.length;
//
//                for(int var8 = 0; var8 < var7; ++var8) {
//                    long offset = var6[var8];
//                    bitsMap.put(offset, true);
//                }
//
//                this.bloomCacheService.setbits(this.getBloomFilterRedisKey(element), bitsMap);
//            } catch (Exception var11) {
//                this.logger.error("insert err key:{},element:{},bloomFilter:{}", new Object[]{this.getBloomFilterRedisKeyPrefix(), element, this.bloomFilter, var11});
//            }
//
//        }
//    }
//
//    private boolean inSafeTime() {
//        int hour = Calendar.getInstance().get(11);
//        return hour < 4;
//    }
//
//    private String getBloomFilterRedisKey(String element) {
//        return this.getBloomFilterRedisKeyPrefix() + ":" + Math.abs(Objects.hashCode(element) % this.getPartCount());
//    }
//
//    private String getBloomFilterRedisKey(int part) {
//        return this.getBloomFilterRedisKeyPrefix() + ":" + part;
//    }
//
//    public boolean mayExist(String element) {
//        if (element == null) {
//            this.logger.error("exist param is null,({})", this.getBloomFilterRedisKeyPrefix());
//            return false;
//        } else {
//            try {
//                long[] offsets = this.bloomFilter.murmurHashOffset(element);
//                List<Boolean> bitsResult = this.bloomCacheService.getbits(this.getBloomFilterRedisKey(element), offsets);
//                Iterator var4 = bitsResult.iterator();
//
//                boolean bitResult;
//                do {
//                    if (!var4.hasNext()) {
//                        return true;
//                    }
//
//                    bitResult = (Boolean)var4.next();
//                } while(bitResult);
//
//                return false;
//            } catch (Exception var6) {
//                this.logger.error("mayExist err key:{},element:{},bloomFilter:{}", new Object[]{this.getBloomFilterRedisKeyPrefix(), element, this.bloomFilter, var6});
//                return false;
//            }
//        }
//    }
//
//    private List<List<String>> splitElement(List<String> elements) {
//        List<List<String>> elementListList = new ArrayList(this.getPartCount());
//
//        for(int i = 0; i < this.getPartCount(); ++i) {
//            elementListList.add(new ArrayList());
//        }
//
//        Iterator var8 = elements.iterator();
//
//        while(var8.hasNext()) {
//            String element = (String)var8.next();
//            int index = Math.abs(Objects.hashCode(element) % this.getPartCount());
//            List<String> list = (List)elementListList.get(index);
//            if (list == null) {
//                List<String> list = new ArrayList();
//                list.add(element);
//                elementListList.set(index, list);
//            } else {
//                list.add(element);
//            }
//        }
//
//        return elementListList;
//    }
//
//    public List<String> mGetMayExist(List<String> elements) {
//        List<List<String>> elementListList = this.splitElement(elements);
//        List<AbstractRedisBloomFilterCache.MGetMayExistTask> taskList = Lists.newArrayList();
//
//        List list;
//        for(int i = 0; i < elementListList.size(); ++i) {
//            list = (List)elementListList.get(i);
//            if (CollectionUtils.isNotEmpty(list)) {
//                taskList.add(new AbstractRedisBloomFilterCache.MGetMayExistTask(list, i));
//            }
//        }
//
//        try {
//            List<String> resultList = Lists.newArrayList();
//            list = this.batchThreadPool.invokeAll(taskList);
//            Iterator var6 = list.iterator();
//
//            while(var6.hasNext()) {
//                Future<List<String>> future = (Future)var6.next();
//                resultList.addAll((Collection)future.get());
//            }
//
//            return resultList;
//        } catch (InterruptedException var8) {
//            this.logger.error("mGetMayExist: " + elements, var8);
//            Thread.currentThread().interrupt();
//        } catch (ExecutionException var9) {
//            this.logger.error("mGetMayExist: " + elements, var9);
//        }
//
//        return Collections.emptyList();
//    }
//
//    public void batchInsert(List<String> elements, boolean init) {
//        if (CollectionUtils.isEmpty(elements)) {
//            this.logger.error("batchInsert param is null,({},{},{})", new Object[]{this.getBloomFilterRedisKeyPrefix(), elements, this.bloomFilter});
//        } else {
//            if (!this.isOpen() && !init) {
//                this.logger.error("batchInsert param, but not open bloom cn.techwolf.boss.bloom.filter, ({})", this.getBloomFilterRedisKeyPrefix());
//            }
//
//            List<List<String>> elementListList = this.splitElement(elements);
//            List<Future> futureList = Lists.newArrayList();
//
//            for(int i = 0; i < elementListList.size(); ++i) {
//                List<String> list = (List)elementListList.get(i);
//                if (CollectionUtils.isNotEmpty(list)) {
//                    futureList.add(this.batchThreadPool.submit(new AbstractRedisBloomFilterCache.BatchInsertTask(list, i)));
//                }
//            }
//
//            Iterator var10 = futureList.iterator();
//
//            while(var10.hasNext()) {
//                Future future = (Future)var10.next();
//
//                try {
//                    future.get();
//                } catch (InterruptedException var8) {
//                    this.logger.error("batch insert " + elementListList, var8);
//                    Thread.currentThread().interrupt();
//                } catch (ExecutionException var9) {
//                    this.logger.error("batch insert " + elementListList, var9);
//                }
//            }
//
//        }
//    }
//
//    private long[] generateOffsetArr(List<String> elements, int hashCnt, BloomFilter bloomFilter) {
//        int resultIdx = 0;
//        long[] result = new long[elements.size() * hashCnt];
//        Iterator var6 = elements.iterator();
//
//        while(var6.hasNext()) {
//            String element = (String)var6.next();
//            long[] offsets = bloomFilter.murmurHashOffset(element);
//            long[] var9 = offsets;
//            int var10 = offsets.length;
//
//            for(int var11 = 0; var11 < var10; ++var11) {
//                long offset = var9[var11];
//                result[resultIdx++] = offset;
//            }
//        }
//
//        return result;
//    }
//
//    public boolean existKey() {
//        for(int i = 0; i < this.getPartCount(); ++i) {
//            if (!this.bloomCacheService.exists(this.getBloomFilterRedisKey(i))) {
//                return false;
//            }
//        }
//
//        return true;
//    }
//
//    public Long strLen() {
//        long length = 0L;
//
//        for(int i = 0; i < this.getPartCount(); ++i) {
//            length += this.bloomCacheService.strlen(this.getBloomFilterRedisKey(i));
//        }
//
//        return length;
//    }
//
//    public void removeKey() {
//        for(int i = 0; i < this.getPartCount(); ++i) {
//            this.bloomCacheService.del(this.getBloomFilterRedisKey(i));
//        }
//
//    }
//
//    public void resetKey() {
//        this.removeKey();
//
//        for(int i = 0; i < this.getPartCount(); ++i) {
//            this.bloomCacheService.setbit(this.getBloomFilterRedisKey(i), 0L, false);
//        }
//
//    }
//
//    private class BatchInsertTask implements Runnable {
//        private final List<String> list;
//        private final int part;
//
//        public BatchInsertTask(List<String> list, int part) {
//            this.list = list;
//            this.part = part;
//        }
//
//        public void run() {
//            this.batchInsert(this.list, this.part);
//        }
//
//        private void batchInsert(List<String> elements, int part) {
//            int hashCnt = AbstractRedisBloomFilterCache.this.bloomFilter.getHashFunctionCount();
//
//            try {
//                long[] offsets = AbstractRedisBloomFilterCache.this.generateOffsetArr(elements, hashCnt, AbstractRedisBloomFilterCache.this.bloomFilter);
//                Map<Long, Boolean> bitsResultMap = new HashMap(offsets.length);
//                long[] var6 = offsets;
//                int var7 = offsets.length;
//
//                for(int var8 = 0; var8 < var7; ++var8) {
//                    long offset = var6[var8];
//                    bitsResultMap.put(offset, true);
//                }
//
//                AbstractRedisBloomFilterCache.this.bloomCacheService.setbits(AbstractRedisBloomFilterCache.this.getBloomFilterRedisKey(part), bitsResultMap);
//                AbstractRedisBloomFilterCache.this.logger.info("batchInsert bloomFilterKey:{},offsetSize:{}", AbstractRedisBloomFilterCache.this.bloomFilter, offsets.length);
//            } catch (Exception var11) {
//                AbstractRedisBloomFilterCache.this.logger.error("batchInsert({}, {}, {})", new Object[]{AbstractRedisBloomFilterCache.this.getBloomFilterRedisKeyPrefix(), elements, AbstractRedisBloomFilterCache.this.bloomFilter, var11});
//            }
//
//        }
//    }
//
//    private class MGetMayExistTask implements Callable<List<String>> {
//        private final List<String> list;
//        private final int part;
//
//        public MGetMayExistTask(List<String> list, int part) {
//            this.list = list;
//            this.part = part;
//        }
//
//        public List<String> call() {
//            return this.mGetMayExist(this.list, this.part);
//        }
//
//        private List<String> mGetMayExist(List<String> elements, int part) {
//            int hashCnt = AbstractRedisBloomFilterCache.this.bloomFilter.getHashFunctionCount();
//
//            try {
//                long[] offsets = AbstractRedisBloomFilterCache.this.generateOffsetArr(elements, hashCnt, AbstractRedisBloomFilterCache.this.bloomFilter);
//                List<Boolean> bitsResult = AbstractRedisBloomFilterCache.this.bloomCacheService.getbits(AbstractRedisBloomFilterCache.this.getBloomFilterRedisKey(part), offsets);
//                List<String> mayExistResult = new ArrayList(elements.size() / 10);
//
//                for(int i = 0; i < bitsResult.size(); i += hashCnt) {
//                    if (this.mayExists(bitsResult, i, hashCnt)) {
//                        mayExistResult.add(elements.get(i / hashCnt));
//                    }
//                }
//
//                return mayExistResult;
//            } catch (Exception var8) {
//                AbstractRedisBloomFilterCache.this.logger.error("mGetMayExist({}, {}, {})", new Object[]{AbstractRedisBloomFilterCache.this.getBloomFilterRedisKey(part), elements, AbstractRedisBloomFilterCache.this.bloomFilter, var8});
//                return Collections.emptyList();
//            }
//        }
//
//        private boolean mayExists(List<Boolean> bitsResult, int idx, int hashCnt) {
//            for(int k = idx; k < idx + hashCnt; ++k) {
//                if (!(Boolean)bitsResult.get(k)) {
//                    return false;
//                }
//            }
//
//            return true;
//        }
//    }
//}
//
