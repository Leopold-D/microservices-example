package org.ldauvergne.garage.shared.models;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Ensure having abstract class to allow several Vehicles types
 * 
 * @author Leopold Dauvergne
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleModel {
	// Can be used to update data
	public static Integer GARAGE_VERSION = GarageModel.GARAGE_VERSION;
	@Id
	private String registration_id;
	private Integer level_id;
	private Integer lot_id;

	private VehicleType vehicleType = VehicleType.UNDEFINED;
	private Integer nbWheels;
	private String brand;

	public VehicleModel(VehicleModel lVehicle) {
		BeanUtils.copyProperties(lVehicle, this);
	}
	
	public VehicleModel mAddVehicleTypeFromNbWheels() {
		switch (this.nbWheels) {
		case VehicleType.MOTORBIKE_NB_WHEELS:
			return (new MotorbikeModel(this));
		case VehicleType.CAR_NB_WHEELS:
			return (new CarModel(this));
		default:
			return this;
		}
	}
}
