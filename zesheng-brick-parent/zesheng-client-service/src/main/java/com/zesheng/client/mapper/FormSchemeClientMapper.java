package com.zesheng.client.mapper;

import com.zesheng.client.model.response.FormSchemeListItemVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * C端方案列表（只读 admin_form_schemes）
 */
@Mapper
public interface FormSchemeClientMapper {

    @Select("<script>SELECT s.id, s.name, s.unit_price AS unitPrice, a.full_address AS deliveryAddressText " +
            "FROM admin_form_schemes s " +
            "LEFT JOIN admin_delivery_addresses a ON s.address_id = a.id " +
            "WHERE s.status = 1 " +
            "<if test='keyword != null and keyword != \"\"'>AND s.name LIKE concat('%',#{keyword},'%')</if> " +
            "ORDER BY s.id DESC</script>")
    List<FormSchemeListItemVo> listByKeyword(@Param("keyword") String keyword);

    @Select("SELECT unit_price FROM admin_form_schemes WHERE id = #{schemeId} AND status = 1")
    BigDecimal getUnitPriceBySchemeId(@Param("schemeId") Long schemeId);

    @Select("SELECT name FROM admin_form_schemes WHERE id = #{schemeId}")
    String getSchemeNameBySchemeId(@Param("schemeId") Long schemeId);
}
