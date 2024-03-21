package org.example.gateway.common.constants;

public interface FilterConst {

    String LOAD_BALANCE_FILTER_ID = "load_balance_filter";
    String LOAD_BALANCE_FILTER_NAME = "load_balance_filter";
    int LOAD_BALANCE_FILTER_ORDER = 100;

    String LOAD_BALANCE_KEY = "load_balance";
    String LOAD_BALANCE_STRATEGY_RANDOM = "Random";
    String LOAD_BALANCE_STRATEGY_ROUND_ROBIN = "RoundRobin";


    String ROUTER_FILTER_ID = "router_filter";
    String ROUTER_FILTER_NAME = "router_filter";
    int ROUTER_FILTER_ORDER = Integer.MAX_VALUE - 1;

    String ROUTE_PROTOCOL_HTTP = "http";
    String ROUTE_PROTOCOL_DUBBO = "dubbo";
    String ROUTE_PROTOCOL_GRPC = "grpc";

    String FLOW_CTL_FILTER_ID = "flow_ctl_filter";
    String FLOW_CTL_FILTER_NAME = "flow_ctl_filter";
    int FLOW_CTL_FILTER_ORDER = 50;

    String FLOW_CTL_TYPE_PATH = "path";
    String FLOW_CTL_TYPE_SERVICE = "service";

    String FLOW_CTL_LIMIT_DURATION = "duration"; //以秒为单位
    String FLOW_CTL_LIMIT_PERMITS = "permits"; //允许请求的次数

    String FLOW_CTL_MODEL_DISTRIBUTED = "distributed";
    String FLOW_CTL_MODEL_SINGLETON = "Singleton";

    String USER_AUTH_FILTER_ID = "user_auth_filter";
    String USER_AUTH_FILTER_NAME = "user_auth_filter";
    int USER_AUTH_FILTER_ORDER = 1;

    String GRAY_FILTER_ID = "gray_filter";
    String GRAY_FILTER_NAME = "gray_filter";
    int GRAY_FILTER_ORDER = 1;

    // 最新执行
    String MONITOR_FILTER_ID = "monitor_filter";
    String MONITOR_FILTER_NAME = "monitor_filter";
    int MONITOR_FILTER_ORDER = -1;

    // 最后执行
    String MONITOR_END_FILTER_ID = "monitor_end_filter";
    String MONITOR_END_FILTER_NAME = "monitor_end_filter";
    int MONITOR_END_FILTER_ORDER = Integer.MAX_VALUE;

    String MOCK_FILTER_ID = "mock_filter";
    String MOCK_FILTER_NAME = "mock_filter";
    int MOCK_FILTER_ORDER = 0;
}
