
package com.github.son_daehyeon.domain.project.__sub__.instance.__sub__.matrix.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.github.son_daehyeon.domain.project.__sub__.instance.__sub__.matrix.constant.TimeUnit;
import com.github.son_daehyeon.domain.project.__sub__.instance.__sub__.matrix.dto.internal.InstanceMatrix;
import com.github.son_daehyeon.domain.project.__sub__.instance.exception.InstanceNotFouncException;
import com.github.son_daehyeon.domain.project.__sub__.instance.repository.InstanceRepository;
import com.github.son_daehyeon.domain.project.__sub__.instance.schema.Instance;
import com.github.son_daehyeon.domain.project.__sub__.instance.util.ProxmoxApi;
import com.github.son_daehyeon.domain.project.exception.ProjectNotFoundException;
import com.github.son_daehyeon.domain.project.repository.ProjectRepository;
import com.github.son_daehyeon.domain.project.schema.Project;
import com.github.son_daehyeon.domain.user.schema.User;

import jakarta.annotation.PostConstruct;
import kong.unirest.core.json.JSONArray;
import kong.unirest.core.json.JSONObject;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InstanceMatrixService {

    private final ProjectRepository projectRepository;
    private final InstanceRepository instanceRepository;

    private final ScheduledExecutorService currentMatrixExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService matrixExecutor = Executors.newSingleThreadScheduledExecutor();
    private final Map<SseEmitter, Instance> currentMatrix = new ConcurrentHashMap<>();
    private final Map<SseEmitter, Map.Entry<Instance, TimeUnit>> matrix = new ConcurrentHashMap<>();

    private final ProxmoxApi proxmoxApi;

    @PostConstruct
    public void schedule() {
        currentMatrixExecutor.scheduleWithFixedDelay(() -> currentMatrix.forEach((emitter, instance) -> {
            JSONObject response = proxmoxApi.http("/lxc/%d/status/current".formatted(instance.getVmid()), HttpMethod.GET).orElseThrow().getObject();
            InstanceMatrix matrix = parse(response);

            try {
                emitter.send(matrix);
            } catch (IOException e) {
                currentMatrix.remove(emitter);
            }
        }), 0, 1, java.util.concurrent.TimeUnit.SECONDS);

        matrixExecutor.scheduleWithFixedDelay(() -> matrix.forEach((emitter, entry) -> {
            JSONArray response = proxmoxApi.http("/lxc/%d/rrddata?timeframe=%s".formatted(entry.getKey().getVmid(), entry.getValue().name().toLowerCase()), HttpMethod.GET).orElseThrow().getArray();

            @SuppressWarnings("unchecked")
            List<InstanceMatrix> matrix = ((List<JSONObject>)response.toList()).stream()
                .map(this::parse)
                .toList();

            try {
                emitter.send(matrix);
            } catch (IOException e) {
                currentMatrix.remove(emitter);
            }
        }), 0, 1, java.util.concurrent.TimeUnit.SECONDS);
    }

    public SseEmitter getCurrentMatrix(String projectId, String instanceId, User user) {

        Project project = projectRepository.findById(projectId)
            .filter(p -> p.getParticipants().contains(user))
            .orElseThrow(ProjectNotFoundException::new);

        Instance instance = instanceRepository.findById(instanceId)
            .filter(x -> x.getProject().equals(project))
            .orElseThrow(InstanceNotFouncException::new);

        SseEmitter emitter = new SseEmitter();

        currentMatrix.put(emitter, instance);

        return emitter;
    }

    public SseEmitter getMatrix(String projectId, String instanceId, TimeUnit timeUnit, User user) {

        Project project = projectRepository.findById(projectId)
            .filter(p -> p.getParticipants().contains(user))
            .orElseThrow(ProjectNotFoundException::new);

        Instance instance = instanceRepository.findById(instanceId)
            .filter(x -> x.getProject().equals(project))
            .orElseThrow(InstanceNotFouncException::new);

        SseEmitter emitter = new SseEmitter();

        matrix.put(emitter, Map.entry(instance, timeUnit));

        return emitter;
    }

    private long formatBytes(double bytes, String targetUnit) {

        int unit = switch (targetUnit) {
            case "KB" -> 1;
            case "MB" -> 2;
            case "GB" -> 3;
            case "TB" -> 4;
            default -> 0;
        };

        return (long)Math.ceil(bytes / Math.pow(1024, unit));
    }

    private InstanceMatrix parse(JSONObject data) {

        return InstanceMatrix.builder()
            .time(LocalDateTime.ofEpochSecond((long) parse(data, "time", LocalDateTime.now().toEpochSecond(ZoneOffset.ofHours(9))), 0, ZoneOffset.ofHours(9)))
            .maxCpu(100)
            .currentCpu((long) (parse(data, "cpu", 0) * 100))
            .maxMemory(formatBytes(parse(data, "maxmem", 0), "MB"))
            .currentMemory(formatBytes(parse(data, "mem", 0), "MB"))
            .maxSwap(formatBytes(parse(data, "maxswap", 0), "MB"))
            .currentSwap(formatBytes(parse(data, "swap", 0), "MB"))
            .maxDisk(formatBytes(parse(data, "maxdisk", 0), "GB"))
            .currentDisk(formatBytes(parse(data, "disk", 0), "GB"))
            .diskInput(formatBytes(parse(data, "diskread", 0), "MB"))
            .diskOutput(formatBytes(parse(data, "diskwrite", 0), "MB"))
            .networkInput(formatBytes(parse(data, "netin", 0), "MB"))
            .networkOutput(formatBytes(parse(data, "netout", 0), "MB"))
            .build();
    }

    private double parse(JSONObject data, String key, double def) {

        return data.has(key) ? Math.max(0, Double.parseDouble(data.getString(key))) : def;
    }
}
