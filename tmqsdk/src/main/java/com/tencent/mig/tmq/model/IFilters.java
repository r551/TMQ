package com.tencent.mig.tmq.model;

/**
 * 切换过滤器时，可用的过滤器作为枚举类型实现此接口，通过此方式使tmq框架代码符合开闭原则
 * 注：这种枚举接口的方式在简单系统上做开闭原则这是第一次尝试，应该比工厂模式方案实现的开闭原则要简单
 * @author yoyoqin
 *
 * @param <T> 消息的标识类型
 * @param <M> 消息类型
 */
public interface IFilters<T, M> {
	IFilter<T, M> getFilter();
}
