-- 添加字段
alter table t_device
    add name varchar(255) not null comment '设备名称';
alter table t_device
    add max_camera_num int default 0 not null comment '最大相机数量';

-- 修改 创建人/修改人/创建时间/修改时间 符合jeecg mybiatis 拦截器自动补全
alter table t_device
    change creator create_by varchar(32) null comment '创建人';
alter table t_device
    change modifier update_by varchar(32) null comment '修改人';

alter table t_alarm_record
    change creator create_by varchar(32) null comment '创建人';
alter table t_alarm_record
    change modifier update_by varchar(32) null comment '修改人';

alter table t_algorithm
    change creator create_by varchar(32) null comment '创建人';
alter table t_algorithm
    change modifier update_by varchar(32) null comment '修改人';

alter table t_camera
    change creator create_by varchar(32) null comment '创建人';
alter table t_camera
    change modifier update_by varchar(32) null comment '修改人';

alter table t_camera_ai
    change creator create_by varchar(32) null comment '创建人';
alter table t_camera_ai
    change modifier update_by varchar(32) null comment '修改人';

alter table t_camera_algo_assign
    change creator create_by varchar(32) null comment '创建人';
alter table t_camera_algo_assign
    change modifier update_by varchar(32) null comment '修改人';   