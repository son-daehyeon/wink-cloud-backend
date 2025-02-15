package com.github.son_daehyeon.domain.instance.dto.internal;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record InstanceMatrix(

	LocalDateTime time,

	double maxcpu,
	double cpu,

	double maxmem,
	double mem,

	double maxdisk,
	double disk,

	double diskread,
	double diskwrite,

	double netin,
	double netout
	) {
}
