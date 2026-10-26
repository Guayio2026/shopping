package com.scaffold.mapper;

import com.scaffold.entity.User;
import com.scaffold.dto.NameCount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserMapper {

    User findById(@Param("id") Long id);

    User findByUsername(@Param("username") String username);

    User findByOpenid(@Param("openid") String openid);

    List<User> findPage(@Param("keyword") String keyword,
                        @Param("role") String role,
                        @Param("offset") int offset,
                        @Param("limit") int limit);

    long countPage(@Param("keyword") String keyword, @Param("role") String role);

    int insert(User user);

    int update(User user);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    int updateBalance(@Param("id") Long id, @Param("balance") BigDecimal balance);

    int deductBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

    int addBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

    // ===== 「账户余额 account」专用（充值 / 支付链路），见 UserMapper.xml 里的注释 =====

    /** 充值：account = account + amount（原子） */
    int addAccount(@Param("id") Long id, @Param("amount") Double amount);

    /**
     * 扣款：account = account - amount，仅当 account >= amount 时才执行（原子）
     * @return 1 = 扣成功；0 = 余额不足（MySQL 一行都没更新）
     */
    int deductAccount(@Param("id")Long id, @Param("amount")Double amount);

    int deleteById(@Param("id") Long id);

    long countAll();

    List<NameCount> countDailyCreated(@Param("days") int days);
}
