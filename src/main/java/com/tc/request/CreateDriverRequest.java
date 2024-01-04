package com.tc.request;

import java.math.BigDecimal;

public record CreateDriverRequest(String firstName, String lastName, BigDecimal salary) {
}
