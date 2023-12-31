package LG_Internship.Server.Ingredient;


import LG_Internship.Server.Device.DeviceEntity;
import LG_Internship.Server.Device.DeviceRepository;
import LG_Internship.Server.Device_Ingredient.DeviceIngredientEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IngredientService {
    private final IngredientRepository ingredientsRepository;
    private final DeviceRepository deviceRepository;

    @Transactional
    public DeviceEntity findDevice(Long deviceId){

        Optional<DeviceEntity> findDevice = deviceRepository.findById(deviceId);
        DeviceEntity device;
        if(findDevice.isEmpty()){
            device = DeviceEntity.builder()
                    .deviceId(deviceId)
                    .build();

            List<IngredientEntity> ingredientList = ingredientsRepository.findAll();

            for(int i = 0; i < ingredientList.size(); i ++){
                DeviceIngredientEntity deviceIngredient = DeviceIngredientEntity.builder()
                        .ingredient(ingredientList.get(i))
                        .device(device)
                        .amount(0)
                        .build();
                device.addDeviceIngredient(deviceIngredient);
            }

            return deviceRepository.save(device);

        }
        device = findDevice.get();
        return device;

    }

    @Transactional
    public List<DeviceIngredientsDTO> refreshIngredient(DeviceEntity device, IngredientsRequest data){

        List<DeviceIngredientEntity> deviceIngredients = device.getDeviceIngredientList();
        List<DeviceIngredientsDTO> deviceIngredientsDTOS = new ArrayList<>();


        Field[] fields = IngredientsRequest.class.getDeclaredFields();

        for (int i = 0; i < deviceIngredients.size(); i++) {
            DeviceIngredientEntity deviceIngredient = deviceIngredients.get(i);
            IngredientEntity ingredient = deviceIngredient.getIngredient();

            //Get Field name

            for (Field field : fields) {
                field.setAccessible(true);
                if (field.getType() == int.class && field.getName().equals(ingredient.getName())) {  // 필드의 타입이 int 인 경우에만 getInt 메서드를 호출합니다.
                    try {
                        if(deviceIngredient.getAmount() < field.getInt((data)))
                            deviceIngredient.setValidDate( LocalDate.now().plusDays( ingredient.getValidDay() ));
                        deviceIngredient.setAmount(field.getInt(data));
                        if(field.getInt(data) == 0) deviceIngredient.setValidDate(null);

                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
            deviceIngredientsDTOS.add(new DeviceIngredientsDTO(
                    ingredient.getName(),
                    ingredient.getIngredientPhoto(),
                    deviceIngredient.getAmount(),
                    deviceIngredient.getValidDate()
            ));
        }
        return deviceIngredientsDTOS;

    }

    @Transactional(readOnly = true)
    public List<DeviceIngredientsDTO> getDeviceIngredientsList(Long deviceId) {
        Optional<DeviceEntity> device = deviceRepository.findById(deviceId);
        if(device.isEmpty()) return null;

        List<DeviceIngredientEntity> deviceIngredients = device.get().getDeviceIngredientList();
        List<DeviceIngredientsDTO> deviceIngredientsDTOS = new ArrayList<>();
        for (int i = 0; i < deviceIngredients.size();i++) {
            DeviceIngredientEntity deviceIngredient = deviceIngredients.get(i);
            IngredientEntity ingredient = deviceIngredient.getIngredient();

            deviceIngredientsDTOS.add(new DeviceIngredientsDTO(
                    ingredient.getName(),
                    ingredient.getIngredientPhoto(),
                    deviceIngredient.getAmount(),
                    deviceIngredient.getValidDate()
            ));
        }
        return deviceIngredientsDTOS;
    }

    public List<IngredientDTO> getIngredientsList(){
       List<IngredientEntity> ingredientList = ingredientsRepository.findAll();
       List<IngredientDTO> ingredientDTOS = new ArrayList<>();
       for(IngredientEntity ingredient : ingredientList){
           ingredientDTOS.add(new IngredientDTO(ingredient.getName(),
                   ingredient.getValidDay(),
                   ingredient.getIngredientPhoto()));
       }

       return ingredientDTOS;
    }
}
