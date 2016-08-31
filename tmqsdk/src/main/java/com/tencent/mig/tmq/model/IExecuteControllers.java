package com.tencent.mig.tmq.model;

/**
 * 切换匹配模式时，可用的匹配模式作为枚举类型实现此接口，通过此方式使tmq框架代码符合开闭原则
 * 注：这种枚举接口的方式在简单系统上做开闭原则这是第一次尝试，应该比工厂模式方案实现的开闭原则要简单
 * @author yoyoqin
 *
 * @param <T> 消息的标识类型
 * @param <M> 消息类型
 */
public interface IExecuteControllers<T, M> {
	IExecuteController<T, M> getController();
}
