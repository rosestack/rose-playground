package io.github.rosestack.spring.boot.security.protect;

import java.util.Set;

public interface AccessListStore {

	Set<String> allowedIps();

	Set<String> deniedIps();

	Set<String> allowedUsernames();

	Set<String> deniedUsernames();
}
