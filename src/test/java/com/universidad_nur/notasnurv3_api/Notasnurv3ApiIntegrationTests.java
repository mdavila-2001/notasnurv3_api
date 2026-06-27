package com.universidad_nur.notasnurv3_api;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
    "com.universidad_nur.notasnurv3_api.controllers",
    "com.universidad_nur.notasnurv3_api.config"
})
class Notasnurv3ApiIntegrationTests {
}
