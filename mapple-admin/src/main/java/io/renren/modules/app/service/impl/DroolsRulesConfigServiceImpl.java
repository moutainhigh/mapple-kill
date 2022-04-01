package io.renren.modules.app.service.impl;

import cn.hutool.core.util.CharsetUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.modules.app.dao.DroolsRulesConfigDao;
import io.renren.modules.app.dao.DroolsRulesDao;
import io.renren.modules.app.entity.UserEntity;
import io.renren.modules.app.entity.drools.DroolsRules;
import io.renren.modules.app.entity.drools.DroolsRulesConfig;
import io.renren.modules.app.entity.drools.UserRuleAction;
import io.renren.modules.app.entity.drools.WhenEntity;
import io.renren.modules.app.service.DroolsRulesConfigService;
import org.apache.commons.lang3.StringUtils;
import org.drools.template.ObjectDataCompiler;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author hxx
 * @date 2022/3/31 10:47
 */
@Service("DroolsRulesConfigService")
public class DroolsRulesConfigServiceImpl extends ServiceImpl<DroolsRulesConfigDao, DroolsRulesConfig> implements DroolsRulesConfigService {

    public static Logger log = LoggerFactory.getLogger(UserRuleAction.class);

    private final DroolsRulesConfigDao droolsRulesConfigDao;

    final
    DroolsRulesDao droolsRulesDao;

    Map<String, String> RulesContainer = new HashMap<>();

    public DroolsRulesConfigServiceImpl(DroolsRulesConfigDao droolsRulesConfigDao, DroolsRulesDao droolsRulesDao) {
        this.droolsRulesConfigDao = droolsRulesConfigDao;
        this.droolsRulesDao = droolsRulesDao;
    }


    @Override
    public void updateRules(DroolsRulesConfig droolsRulesConfig) {
        //修改规则
        if (droolsRulesConfig.getJobValue().equals("true")) {
            droolsRulesConfig.setJobValue(null);
        }
        if (droolsRulesConfig.getOverdueValue().equals("true")) {
            droolsRulesConfig.setOverdueValue(null);
        }
        if (droolsRulesConfig.getDishonestValue().equals("true")) {
            droolsRulesConfig.setDishonestValue(null);
        }
        droolsRulesConfig.setRuleName("fix");
        //更新droolsRulesConfig规则数据表
        updateById(droolsRulesConfig);
        //把规则字符串放入droolsrules表中
        putRulesInDB();
    }

    /**
     * 规则生成以及持久化
     */
    public void putRulesInDB() {
        DroolsRulesConfig droolsRulesConfig = droolsRulesConfigDao.selectOne(new QueryWrapper<DroolsRulesConfig>().eq("rule_name", "fix"));
        //notHasJob==false , isOverdue==false , balance>=20000, isDishonest==false
        //暴力拼接when条件语句
        StringBuilder sb = new StringBuilder("age>=");
        sb.append(droolsRulesConfig.getAgeMin());
        sb.append(" && age<=");
        sb.append(droolsRulesConfig.getAgeMax());
        if (Objects.equals(droolsRulesConfig.getJobValue(), "false")) {
            sb.append(",notHasJob==false");
        }
        if (Objects.equals(droolsRulesConfig.getOverdueValue(), "false")) {
            sb.append(",isOverdue==false");
        }
        if (Objects.equals(droolsRulesConfig.getDishonestValue(), "false")) {
            sb.append(",isDishonest==false");
        }
        sb.append(",balance>=").append(droolsRulesConfig.getBalanceMin());
        //将字符串放入entity中封装
        WhenEntity whenEntity = new WhenEntity();
        whenEntity.setWhenStr(sb.toString());
        List<WhenEntity> list = new ArrayList<>();
        list.add(whenEntity);
        //转换编译
        ObjectDataCompiler converter = new ObjectDataCompiler();
        String drlContent = StringUtils.EMPTY;
        try (InputStream dis = ResourceFactory.
                newClassPathResource("rules/prd/user-rules.drt", CharsetUtil.UTF_8)
                .getInputStream()) {
            //填充模板内容
            drlContent = converter.compile(list, dis);
            log.info("生成的规则内容:{}", drlContent);
        } catch (IOException e) {
            log.info("获取规则模板文件出错:{}", e.getMessage());
        }
        DroolsRules droolsRules = new DroolsRules();
        droolsRules.setRules(drlContent);
        droolsRulesDao.insert(droolsRules);
        //map中缓存一份
        RulesContainer.put("rule", drlContent);
        RulesContainer.put("ruleId", droolsRules.getId());
    }

    /**
     * @param userList
     */
    @Override
    public String filterUsers(List<UserEntity> userList) {
        KieHelper helper = new KieHelper();
        String rule;
        if (!StringUtils.isEmpty(RulesContainer.get("rule"))) {
            rule = RulesContainer.get("rule");
            log.info("规则取自缓存");
        } else {
            DroolsRules droolsRules = droolsRulesDao.selectOne(new QueryWrapper<DroolsRules>().orderByDesc("gmt_create").last("limit 1"));
            rule = droolsRules.getRules();
            RulesContainer.put("rule", rule);
        }
        helper.addContent(rule, ResourceType.DRL);
        KieSession ks = helper.build().newKieSession();
        for (UserEntity userEntity : userList) {
            ks.insert(userEntity);
        }

        int allRules = ks.fireAllRules();
        log.info("成功执行{}条规则", allRules);
        ks.dispose();
        if (allRules==1){
            return "pass";
        }
        return null;
    }
}
