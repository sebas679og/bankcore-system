package com.bankcore.customers.utils.mappers;

import com.bankcore.customers.dto.responses.CustomerDetailsValidateResponse;
import com.bankcore.customers.dto.responses.LoginResponse;
import com.bankcore.customers.dto.responses.RegisterResponse;
import com.bankcore.customers.dto.responses.UserProfileResponse;
import com.bankcore.customers.model.UserEntity;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper responsible for converting {@link UserEntity} objects into response DTOs.
 *
 * <p>Implemented using MapStruct and integrated with Spring's component model for dependency
 * injection.
 *
 * <p>This mapper ensures that only non-sensitive data is exposed to the API layer.
 *
 * @author BankCore Team
 * @author Sebastian Orjuela
 * @author Cristian Ortiz
 * @version 0.1.0
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

  /**
   * Converts a {@link UserEntity} into a {@link RegisterResponse}.
   *
   * <p>The {@code fullName} field is derived from the entity's first and last name using a custom
   * mapping method.
   *
   * @param user the persisted user entity
   * @return a response DTO containing non-sensitive user information
   */
  @Mapping(target = "fullName", source = ".", qualifiedByName = "fullName")
  RegisterResponse toRegisterResponse(UserEntity user);

  /**
   * Maps user details and authentication metadata to a {@link LoginResponse} DTO.
   *
   * <p>This mapping performs the following transformations:
   *
   * <ul>
   *   <li>Maps {@code user.id} to {@code customerId}.
   *   <li>Maps the {@code jwt} parameter to the {@code token} field.
   *   <li>Sets the {@code tokenType} to a constant value of <b>"Bearer"</b>.
   *   <li>Passes the {@code expiresIn} duration directly to the response.
   * </ul>
   *
   * @param user the {@link UserEntity} containing the customer's unique identifier
   * @param jwt the generated JSON Web Token string
   * @param expiresIn the token expiration time in seconds
   * @return a populated {@link LoginResponse} ready for the API consumer
   */
  @Mapping(target = "token", source = "jwt")
  @Mapping(target = "tokenType", constant = "Bearer")
  @Mapping(target = "customerId", source = "user.id")
  LoginResponse toLoginResponse(UserEntity user, String jwt, Long expiresIn);

  /**
   * Maps a {@link UserEntity} to a {@link UserProfileResponse} DTO.
   *
   * <p>This method transforms the internal database entity into a client-facing response. It
   * handles the conversion of specific fields, such as mapping the internal creation date to the
   * standardized response format.
   *
   * @param user The {@link UserEntity} containing the data from the persistence layer.
   * @return A {@link UserProfileResponse} populated with the user's information.
   * @see UserEntity
   * @see UserProfileResponse
   */
  @Mapping(source = "createdDate", target = "createdAt")
  UserProfileResponse toUserProfileResponse(UserEntity user);

  /**
   * Converts a {@link UserEntity} into a {@link CustomerDetailsValidateResponse}.
   *
   * <p>The {@code fullName} field is derived from the entity's first and last name using a custom
   * mapping method.
   *
   * @param user the persisted user entity
   * @return a response DTO containing non-sensitive user information
   */
  @Mapping(target = "fullName", source = ".", qualifiedByName = "fullName")
  CustomerDetailsValidateResponse toCustomerDetailsValidateResponse(UserEntity user);

  /**
   * Generates the full name of the user by concatenating non-null first and last name values.
   *
   * @param user the user entity
   * @return a formatted full name string without null components
   */
  @Named("fullName")
  default String mapFullName(UserEntity user) {
    return Stream.of(user.getFirstName(), user.getLastName())
        .filter(Objects::nonNull)
        .collect(Collectors.joining(" "));
  }
}
