package com.k_int.pushKb.model.responses;

import io.micronaut.serde.annotation.Serdeable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Data Transfer Object representing the confirmation details after a manual task reset.
 * <p>
 * This DTO is returned to the client to provide the status of the operation and
 * identify which specific task was affected.
 */
@Data
@Builder
@Serdeable
@Schema(name = "TaskResetResponse", description = "Confirmation details after a manual task reset")
public class TaskResetDTO {

	/**
	 * The overall status of the reset operation (e.g., "success").
	 */
	@Schema(description = "The status of the reset operation", example = "success")
	String status;

	/**
	 * The unique identifier of the Duty Cycle Task that was reset.
	 */
	@Schema(description = "The UUID of the reset task", example = "550e8400-e29b-41d4-a716-446655440000")
	UUID id;

	/**
	 * A human-readable message describing the outcome of the reset request.
	 */
	@Schema(description = "A descriptive message about the reset outcome", example = "Task status has been reset to IDLE")
	String message;
}
