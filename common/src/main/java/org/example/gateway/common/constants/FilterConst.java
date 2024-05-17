package org.example.gateway.common.constants;

public interface FilterConst {

    String LOAD_BALANCE_FILTER_ID = "loadBalance";
    String LOAD_BALANCE_FILTER_NAME = "load_balance_filter";
    int LOAD_BALANCE_FILTER_ORDER = 100;

    String LOAD_BALANCE_STRATEGY_RANDOM = "random";
    String LOAD_BALANCE_STRATEGY_ROUND_ROBIN = "roundRobin";


    String ROUTER_FILTER_ID = "route";
    String ROUTER_FILTER_NAME = "router_filter";
    int ROUTER_FILTER_ORDER = Integer.MAX_VALUE - 1;

    String ROUTE_PROTOCOL_HTTP = "http";
    String ROUTE_PROTOCOL_DUBBO = "dubbo";
    String ROUTE_PROTOCOL_GRPC = "grpc";

    String FLOW_CTL_FILTER_ID = "flowCtl";
    String FLOW_CTL_FILTER_NAME = "flow_ctl_filter";
    int FLOW_CTL_FILTER_ORDER = 50;

    String FLOW_CTL_TYPE_PATH = "path";
    String FLOW_CTL_TYPE_SERVICE = "service";

    String FLOW_CTL_LIMIT_DURATION = "duration"; //以秒为单位
    String FLOW_CTL_LIMIT_PERMITS = "permits"; //允许请求的次数

    String FLOW_CTL_MODEL_DISTRIBUTED = "distributed";
    String FLOW_CTL_MODEL_SINGLETON = "singleton";

    String USER_AUTH_FILTER_ID = "auth";
    String USER_AUTH_FILTER_NAME = "user_auth_filter";
    int USER_AUTH_FILTER_ORDER = 1;

    String GRAY_FILTER_ID = "gray";
    String GRAY_FILTER_NAME = "gray_filter";
    int GRAY_FILTER_ORDER = 1;

    // 最新执行
    String MONITOR_FILTER_ID = "monitor";
    String MONITOR_FILTER_NAME = "monitor_filter";
    int MONITOR_FILTER_ORDER = -1;

    // 最后执行
    String MONITOR_END_FILTER_ID = "monitorEnd";
    String MONITOR_END_FILTER_NAME = "monitor_end_filter";
    int MONITOR_END_FILTER_ORDER = Integer.MAX_VALUE;

    // mock过滤器
    String MOCK_FILTER_ID = "mock";
    String MOCK_FILTER_NAME = "mock_filter";
    int MOCK_FILTER_ORDER = 0;

    String HYSTRIX_ID = "hystrix";
    String HYSTRIX_NAME = "hystrix_filter";

    String STRIP_PREFIX_FILTER_ID = "stripPrefix";
    String STRIP_PREFIX_FILTER_NAME = "strip_prefix_filter";
    int STRIP_PREFIX_FILTER_ORDER = 10;

    String PREFIX_PATH_FILTER_ID = "prefixPath";
    String PREFIX_PATH_FILTER_NAME = "prefix_path_filter";
    int PREFIX_PATH_FILTER_ORDER = 11;
}
