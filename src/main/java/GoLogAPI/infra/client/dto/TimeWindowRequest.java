package GoLogAPI.infra.client.dto;

public record TimeWindowRequest(
        String startTime,
        String endTime
) { }
