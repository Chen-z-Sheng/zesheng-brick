-- ==============================
-- 初始化数据
-- ==============================

-- 角色
INSERT INTO `sys_role` (`name`,`code`,`status`,`description`)
VALUES
('管理员','admin',1,'系统内置管理员'),
('普通用户','user',1,'普通用户角色')
ON DUPLICATE KEY UPDATE
  name=VALUES(name),status=VALUES(status),description=VALUES(description);

-- 权限点（格式：domain:resource:action，如 admin:recycle-market:list）
INSERT INTO `sys_permission` (`code`,`resource`,`action`,`description`)
VALUES
-- 超级管理员（用于演示页、无对应后端模块的菜单）
('admin','system','admin','超级管理员，可见所有菜单'),
-- 管理端用户
('admin:user:list','admin:user','list','用户列表'),
('admin:user:read','admin:user','read','查看用户详情'),
('admin:user:create','admin:user','create','新增用户'),
('admin:user:update','admin:user','update','修改用户'),
('admin:user:delete','admin:user','delete','删除用户'),
-- 行情管理
('admin:recycle-market:list','admin:recycle-market','list','行情列表/查询'),
('admin:recycle-market:add','admin:recycle-market','add','新增行情'),
('admin:recycle-market:update','admin:recycle-market','update','修改行情'),
('admin:recycle-market:delete','admin:recycle-market','delete','删除行情'),
-- 系统-权限管理
('sys:permission:list','sys:permission','list','权限列表/分页'),
('sys:permission:info','sys:permission','info','权限详情'),
('sys:permission:add','sys:permission','add','新增权限'),
('sys:permission:update','sys:permission','update','修改权限'),
('sys:permission:delete','sys:permission','delete','删除权限'),
-- 系统-角色管理
('sys:role:list','sys:role','list','角色列表/分页'),
('sys:role:info','sys:role','info','角色详情'),
('sys:role:add','sys:role','add','新增角色'),
('sys:role:update','sys:role','update','修改角色'),
('sys:role:delete','sys:role','delete','删除角色'),
-- 系统-角色权限关联
('sys:role-permission:list','sys:role-permission','list','角色权限列表/分页'),
('sys:role-permission:info','sys:role-permission','info','角色权限详情'),
('sys:role-permission:add','sys:role-permission','add','新增角色权限'),
('sys:role-permission:update','sys:role-permission','update','修改角色权限'),
('sys:role-permission:delete','sys:role-permission','delete','删除角色权限'),
-- 帮助FAQ
('admin:help-faq:list','admin:help-faq','list','FAQ列表/查询'),
('admin:help-faq:add','admin:help-faq','add','新增FAQ'),
('admin:help-faq:update','admin:help-faq','update','修改FAQ'),
('admin:help-faq:delete','admin:help-faq','delete','删除FAQ'),
-- 用户问题反馈
('admin:user-feedback:list','admin:user-feedback','list','用户问题反馈列表/查询'),
('admin:user-feedback:reply','admin:user-feedback','reply','回复用户问题反馈'),
-- 系统配置
('admin:config:list','admin:config','list','配置列表/查询'),
('admin:config:add','admin:config','add','新增配置'),
('admin:config:update','admin:config','update','修改配置'),
('admin:config:delete','admin:config','delete','删除配置'),
-- 方案
('admin:form-scheme:list','admin:form-scheme','list','方案列表/查询'),
('admin:form-scheme:add','admin:form-scheme','add','新增方案'),
('admin:form-scheme:update','admin:form-scheme','update','修改方案'),
('admin:form-scheme:delete','admin:form-scheme','delete','删除方案'),
-- 下单地址
('admin:delivery-address:list','admin:delivery-address','list','下单地址列表/查询'),
('admin:delivery-address:add','admin:delivery-address','add','新增下单地址'),
('admin:delivery-address:update','admin:delivery-address','update','修改下单地址'),
('admin:delivery-address:delete','admin:delivery-address','delete','删除下单地址'),
-- 物流公司
('admin:logistics-company:list','admin:logistics-company','list','物流公司列表/查询'),
('admin:logistics-company:add','admin:logistics-company','add','新增物流公司'),
('admin:logistics-company:update','admin:logistics-company','update','修改物流公司'),
('admin:logistics-company:delete','admin:logistics-company','delete','删除物流公司'),
-- 公告管理
('admin:announcement:list','admin:announcement','list','公告列表/查询'),
('admin:announcement:add','admin:announcement','add','新增公告'),
('admin:announcement:update','admin:announcement','update','修改公告'),
('admin:announcement:delete','admin:announcement','delete','删除公告'),
-- 表单提交
('admin:form-submission:list','admin:form-submission','list','表单提交列表/查询'),
('admin:form-submission:update','admin:form-submission','update','修改表单提交'),
-- 行情报单提交
('admin:sell-order-submission:list','admin:sell-order-submission','list','行情报单提交列表/查询'),
('admin:sell-order-submission:update','admin:sell-order-submission','update','修改行情报单提交'),
-- 定时任务
('sys:job-task:list','sys:job-task','list','定时任务列表/查询'),
('sys:job-task:add','sys:job-task','add','新增定时任务'),
('sys:job-task:update','sys:job-task','update','修改定时任务'),
('sys:job-task:delete','sys:job-task','delete','删除定时任务'),
-- 待办任务
('sys:todo-task:list','sys:todo-task','list','待办任务列表/查询'),
('sys:todo-task:add','sys:todo-task','add','新增待办任务'),
('sys:todo-task:update','sys:todo-task','update','修改待办任务'),
('sys:todo-task:delete','sys:todo-task','delete','删除待办任务')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- 绑定：管理员 -> 全部权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`,`permission_id`)
SELECT r.id, p.id FROM sys_role r JOIN sys_permission p WHERE r.code='admin';

-- 绑定：普通用户 -> 只读
INSERT IGNORE INTO `sys_role_permission` (`role_id`,`permission_id`)
SELECT r.id, p.id FROM sys_role r JOIN sys_permission p
WHERE r.code='user' AND p.code IN ('admin:user:list','admin:user:read');

-- 管理员账号
SET @pwd := '$2b$10$dqDyBzKVXfURu6wgg/R4LOKKV2Wx1NS/yiAiRosVQqooqAUgEG3Vq'; -- bcrypt("123456")
INSERT INTO `admin_user` (`username`,`password_hash`,`role_id`,`phone`,`avatar_url`,`status`,`remark`)
SELECT 'admin', @pwd, r.id, '18984533503',
'https://img2.baidu.com/it/u=2913587275,3049265006&fm=253&app=138&f=JPEG?w=500&h=500',
1, '超级管理员'
FROM `sys_role` r
WHERE r.code='admin'
  AND NOT EXISTS (SELECT 1 FROM `admin_user` WHERE `username`='admin');

-- ==============================
-- 校验结果
-- ==============================
SELECT '角色总数' AS type, COUNT(*) FROM sys_role
UNION ALL
SELECT '权限总数', COUNT(*) FROM sys_permission
UNION ALL
SELECT '绑定关系数', COUNT(*) FROM sys_role_permission
UNION ALL
SELECT '用户总数', COUNT(*) FROM admin_user;