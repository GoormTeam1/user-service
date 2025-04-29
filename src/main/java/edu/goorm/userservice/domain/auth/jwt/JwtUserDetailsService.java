package edu.goorm.userservice.domain.auth.jwt;


import edu.goorm.userservice.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email)
      throws UsernameNotFoundException {

    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));

    return User.builder()
        .username(user.getEmail())
        .password(user.getPassword())
        .roles(user.getRole().replace("ROLE_", ""))
        .build();
  }
}
