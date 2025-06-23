package liaison.groble.mapping.market;

import org.mapstruct.Mapper;

import liaison.groble.api.model.maker.response.ContactInfoResponse;
import liaison.groble.api.model.maker.response.MakerIntroSectionResponse;
import liaison.groble.application.market.dto.ContactInfoDTO;
import liaison.groble.application.market.dto.MarketIntroSectionDTO;
import liaison.groble.mapping.config.GrobleMapperConfig;

@Mapper(config = GrobleMapperConfig.class)
public interface MarketMapper {
  // ====== 📤 DTO → Response 변환 ======
  MakerIntroSectionResponse toMakerIntroSectionResponse(
      MarketIntroSectionDTO marketIntroSectionDTO);

  ContactInfoResponse toContactInfoResponse(ContactInfoDTO contactInfoDTO);
}
