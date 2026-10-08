package io.github.theagniayush.ledgerpay.account.repository;

import io.github.theagniayush.ledgerpay.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
}
