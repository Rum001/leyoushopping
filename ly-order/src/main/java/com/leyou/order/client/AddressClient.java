package com.leyou.order.client;

import com.leyou.order.dto.AddressDTO;

import java.util.ArrayList;
import java.util.List;

public abstract class AddressClient {
    public static final List<AddressDTO>addressList=new ArrayList<AddressDTO>(){
        {
          AddressDTO address=new AddressDTO();
          address.setId(1L);
          address.setCity("岳阳");
          address.setDistrict("君山区");
          address.setName("哈巴威");
          address.setAddress("岩谷伙计旁");
          address.setPhone("15250998245");
          address.setState("湖南省");
          address.setZipCode("3306021");
          address.setIsDefault(false);
          add(address);

            AddressDTO address2=new AddressDTO();
            address2.setId(2L);
            address2.setCity("岳阳");
            address2.setDistrict("君山区");
            address2.setName("哈巴胖");
            address.setAddress("岩谷伙计旁");
            address2.setPhone("15250998245");
            address2.setState("湖南省");
            address2.setZipCode("3306021");
            address2.setIsDefault(false);
            add(address2);
        }
    };

    public static AddressDTO findById(Long id){
        for (AddressDTO addressDTO : addressList) {
            if(addressDTO.getId()==id){
                return addressDTO;
            }
        }
        return null;
    }

}
