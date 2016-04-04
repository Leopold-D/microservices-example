package garage.api.service;
import java.util.Arrays;
import java.util.Date;

import org.ldauvergne.garage.shared.models.GarageModel;
import org.ldauvergne.garage.shared.models.LevelModel;
import org.ldauvergne.garage.shared.models.VehicleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import garage.api.utils.APIUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
/**
 * 
 * @author Leopold Dauvergne
 *
 */
@RestController
@RequestMapping("api")
@ComponentScan("org.ldauvergne.garage.api.service")
public class GarageAPIFacadeService {

	private static final Logger LOG = LoggerFactory.getLogger(GarageAPIFacadeService.class);

	private static final String CORE_SERVICE_URL = "http://garage-core/core/";

	@Autowired
	APIUtils aAPIUtils;

	@Autowired
	GarageAPIService aGarageAPIService;
	
	@Autowired
	@LoadBalanced
	private RestTemplate aRestTemplate;

	@ApiOperation(value = "mHello", nickname = "mHello")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = String.class)}) 
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String mHello() {
		String lCoreHello = aRestTemplate.getForObject(CORE_SERVICE_URL, String.class);
		return "{\"timestamp\":\"" + new Date() + "\",\"content\":\"Hello from Garage API Service\"}" + "<br>"
				+ lCoreHello;
	}

	/**
	 * Vehicle enters
	 * 
	 * @param registration_id
	 * @param pVehicle
	 * @return
	 */
	@ApiOperation(value = "mEnter", nickname = "mEnter")
    @ApiResponses(value = { 
            @ApiResponse(code = 200, message = "Success", response = VehicleModel.class)}) 
	@RequestMapping(value = "/clients/gate", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public ResponseEntity<Object> mEnter(@RequestBody VehicleModel pVehicle) {

		LOG.info("\n\nIncoming Vehicle");
		try {
			LOG.info("Check Vehicle");
			pVehicle = aGarageAPIService.mCheckVehicle(pVehicle);
			LOG.info("Vehicle Type: " + pVehicle.getVehicleType().toString());
		} catch (Exception e) {
			return aAPIUtils.createResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		
		HttpHeaders lHeaders = new HttpHeaders();
		lHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		lHeaders.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<VehicleModel> lEntity = new HttpEntity<VehicleModel>(pVehicle, lHeaders);

		try {
			ResponseEntity<Object> lResult = aRestTemplate.exchange(CORE_SERVICE_URL + "/clients/gate", HttpMethod.POST,
					lEntity, Object.class);

			return lResult;
		} catch (HttpClientErrorException e) {
			return aAPIUtils.createResponse(e.getResponseBodyAsString(), e.getStatusCode());
		}
	}

	/**
	 * Vehicle exits
	 * 
	 * @param pRegistrationId
	 * @return
	 */
	@ApiOperation(value = "mExit", nickname = "mExit")
	@RequestMapping(value = "/clients/gate/{registration_id}", method = RequestMethod.DELETE)
	public ResponseEntity<Object> mExit(@PathVariable("registration_id") String pRegistrationId) {

		LOG.info("Checking if Vehicle Plate is valid");
		if (!aGarageAPIService.mIsValidPlate(pRegistrationId)) {
			return aAPIUtils.createResponse(GarageAPIService.INVALID_LICENCE_PLATE_MESSAGE,
					HttpStatus.FORBIDDEN);
		}
		
		try {
			ResponseEntity<Object> lResult = aRestTemplate.exchange(CORE_SERVICE_URL + "/clients/gate/" + pRegistrationId,
					HttpMethod.DELETE, new HttpEntity<Object>(null, null), Object.class);

			return lResult;
		} catch (HttpClientErrorException e) {
			return aAPIUtils.createResponse(e.getResponseBodyAsString(), e.getStatusCode());
		}
	}

	/**
	 * Find Vehicle
	 * 
	 * @param pRegistrationId
	 * @return
	 */
	@ApiOperation(value = "mFind", nickname = "mFind")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success", response = VehicleModel.class)}) 
	@RequestMapping(value = "/clients/find/{registration_id}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> mFind(@PathVariable("registration_id") String pRegistrationId) {

		LOG.info("Checking if Vehicle Plate is valid");
		if (!aGarageAPIService.mIsValidPlate(pRegistrationId)) {
			return aAPIUtils.createResponse(GarageAPIService.INVALID_LICENCE_PLATE_MESSAGE,
					HttpStatus.FORBIDDEN);
		}
		
		HttpHeaders lHeaders = new HttpHeaders();
		lHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		try {
			ResponseEntity<Object> result = aRestTemplate.exchange(CORE_SERVICE_URL + "/clients/find/" + pRegistrationId,
					HttpMethod.GET, new HttpEntity<Object>(null, lHeaders), Object.class);

			return result;
		} catch (HttpClientErrorException e) {
			return aAPIUtils.createResponse(e.getResponseBodyAsString(), e.getStatusCode());
		}
	}

	/**
	 * Returns garage status
	 * 
	 * @param garage
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@ApiOperation(value = "mGetStatus", nickname = "mGetStatus")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success", response = GarageModel.class)}) 
	@RequestMapping(value = "/admin/status", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> mGetStatus() {

		HttpHeaders lHeaders = new HttpHeaders();
		lHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		lHeaders.setContentType(MediaType.APPLICATION_JSON);
		try {
			ResponseEntity<Object> lResult = aRestTemplate.exchange(CORE_SERVICE_URL + "/admin/status",
					HttpMethod.GET, new HttpEntity<Object>(null, lHeaders), Object.class);

			return lResult;
		} catch (HttpClientErrorException e) {
			return aAPIUtils.createResponse(e.getResponseBodyAsString(), e.getStatusCode());
		}
	}

	/**
	 * Build garage
	 * 
	 * @param lGarageLevels
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@ApiOperation(value = "mBuild", nickname = "mBuild")
	@RequestMapping(value = "/admin/build/garage", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity<Object> mBuild(@RequestBody LevelModel[] lGarageLevels) {
		
		HttpHeaders lHeaders = new HttpHeaders();
		lHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		lHeaders.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<LevelModel[]> lEntity = new HttpEntity<LevelModel[]>(lGarageLevels, lHeaders);
		
		try {
			ResponseEntity<Object> lResult = aRestTemplate.exchange(CORE_SERVICE_URL + "/admin/build/garage",
					HttpMethod.POST, lEntity, Object.class);
			return lResult;
		} catch (HttpClientErrorException e) {
			return aAPIUtils.createResponse(e.getResponseBodyAsString(), e.getStatusCode());
		}
	}

	/**
	 * Delete garage
	 * 
	 * @param level_id
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@ApiOperation(value = "mDeleteGarage", nickname = "mDeleteGarage")
	@RequestMapping(value = "/admin/build/garage", method = RequestMethod.DELETE)
	public ResponseEntity<Object> mDeleteGarage() {

		ResponseEntity<Object> lResult = aRestTemplate.exchange(CORE_SERVICE_URL + "/admin/build/garage",
				HttpMethod.DELETE, new HttpEntity<Object>(null, null), Object.class);

		return lResult;
	}

	/**
	 * Add a level to the garage
	 * 
	 * @param level
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@ApiOperation(value = "mAddLevel", nickname = "mAddLevel")
	@RequestMapping(value = "/admin/build/level", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity<Object> mAddLevel(@RequestBody LevelModel level) {

		HttpHeaders lHeaders = new HttpHeaders();
		lHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		lHeaders.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<LevelModel> lEntity = new HttpEntity<LevelModel>(level, lHeaders);

		try {
			ResponseEntity<Object> lResult = aRestTemplate.exchange(CORE_SERVICE_URL + "/admin/build/level",
					HttpMethod.POST, lEntity, Object.class);

			return lResult;
		} catch (HttpClientErrorException e) {
			return aAPIUtils.createResponse(e.getResponseBodyAsString(), e.getStatusCode());
		}
	}

	/**
	 * Modify a level in the garage, ie : remotes
	 * 
	 * @param level_id
	 * @param level
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@ApiOperation(value = "mModifyLevel", nickname = "mModifyLevel")
	@ApiResponses(value = { 
			@ApiResponse(code = 204, message = "No Content")}) 
	@RequestMapping(value = "/admin/build/level/{level_id}", method = RequestMethod.PUT, consumes = "application/json")
	public ResponseEntity<Object> mModifyLevel(@PathVariable("level_id") Long level_id,
			@RequestBody LevelModel level) {

		HttpHeaders lHeaders = new HttpHeaders();
		lHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		lHeaders.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<LevelModel> lEntity = new HttpEntity<LevelModel>(level, lHeaders);

		try {
			ResponseEntity<Object> lResult = aRestTemplate.exchange(CORE_SERVICE_URL + "/admin/build/level/" + level_id,
					HttpMethod.PUT, lEntity, Object.class);

			return lResult;
		} catch (HttpClientErrorException e) {
			return aAPIUtils.createResponse(e.getResponseBodyAsString(), e.getStatusCode());
		}
	}

	/**
	 * Delete a level, ie : town hasn't approved to build so high/low !
	 * 
	 * @param level_id
	 * @return
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@ApiOperation(value = "mDeleteLevel", nickname = "mDeleteLevel")
	@RequestMapping(value = "/admin/build/level", method = RequestMethod.DELETE)
	public ResponseEntity<Object> mDeleteLevel() {

		HttpHeaders lHeaders = new HttpHeaders();
		HttpEntity<Object> lEntity = new HttpEntity<Object>(null, lHeaders);

		try {
			ResponseEntity<Object> lResult = aRestTemplate.exchange(CORE_SERVICE_URL + "/admin/build/level",
					HttpMethod.DELETE, lEntity, Object.class);

			return lResult;
		} catch (HttpClientErrorException e) {
			return aAPIUtils.createResponse(e.getResponseBodyAsString(), e.getStatusCode());
		}
	}

}
