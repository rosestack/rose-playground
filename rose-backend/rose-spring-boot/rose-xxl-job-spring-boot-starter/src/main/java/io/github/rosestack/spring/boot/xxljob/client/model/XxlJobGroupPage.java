package io.github.rosestack.spring.boot.xxljob.client.model;

import lombok.Data;

import java.util.List;

@Data
public class XxlJobGroupPage {

	private Long recordsFiltered;

	private Long recordsTotal;

	private List<XxlJobGroup> data;
}
