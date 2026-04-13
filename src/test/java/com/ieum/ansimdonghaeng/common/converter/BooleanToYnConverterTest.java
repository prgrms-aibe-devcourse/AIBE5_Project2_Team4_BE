package com.ieum.ansimdonghaeng.common.converter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BooleanToYnConverterTest {

    private BooleanToYnConverter converter;

    @BeforeEach
    void setUp() {
        converter = new BooleanToYnConverter();
    }

    @Test
    void convertToDatabaseColumnReturnsYForTrue() {
        assertThat(converter.convertToDatabaseColumn(true)).isEqualTo("Y");
    }

    @Test
    void convertToDatabaseColumnReturnsNForFalse() {
        assertThat(converter.convertToDatabaseColumn(false)).isEqualTo("N");
    }

    @Test
    void convertToEntityAttributeTreatsOnlyYAsTrue() {
        assertThat(converter.convertToEntityAttribute("Y")).isTrue();
        assertThat(converter.convertToEntityAttribute("N")).isFalse();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }
}
