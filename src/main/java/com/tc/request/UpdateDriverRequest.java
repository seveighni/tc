package com.tc.request;

import java.math.BigDecimal;

public record UpdateDriverRequest(String firstName, String lastName, BigDecimal salary) {
}
