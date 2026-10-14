/*
 * Copyright (c) 2026 BasaltMC.
 * Licensed under MIT; see LICENSE:
 * https://github.com/BasaltProject/Shim/blob/main/LICENSE
 * SPDX-License-Identifier: MIT
 */

#ifndef SHIM_FLOATING_FORMAT_H
#define SHIM_FLOATING_FORMAT_H

#include <stdint.h>
#include <string.h>

namespace shim {
namespace decimal {

// IEEE binary64 conversion needs fewer than 1,200 bits in these comparisons.
struct Natural {
    uint32_t words[40];
    unsigned length;
    explicit Natural(uint64_t value = 0) : length(value > 0xffffffffULL ? 2 : 1) {
        memset(words, 0, sizeof(words));
        words[0] = uint32_t(value);
        words[1] = uint32_t(value >> 32);
    }
    int compare(const Natural& other) const {
        if (length != other.length) return length < other.length ? -1 : 1;
        for (unsigned i = length; i--;) {
            if (words[i] != other.words[i]) return words[i] < other.words[i] ? -1 : 1;
        }
        return 0;
    }
    void multiply(unsigned factor) {
        uint64_t carry = 0;
        for (unsigned i = 0; i < length; ++i) {
            uint64_t value = uint64_t(words[i]) * factor + carry;
            words[i] = uint32_t(value);
            carry = value >> 32;
        }
        if (carry) words[length++] = uint32_t(carry);
    }
    void power10(unsigned exponent) {
        while (exponent--) multiply(10);
    }
    void shift(unsigned bits) {
        while (bits >= 29) { multiply(1u << 29); bits -= 29; }
        if (bits) multiply(1u << bits);
    }
    void subtract(const Natural& other) {
        uint64_t borrow = 0;
        for (unsigned i = 0; i < length; ++i) {
            uint64_t sub = uint64_t(i < other.length ? other.words[i] : 0) + borrow;
            uint64_t original = words[i];
            words[i] = uint32_t(original - sub);
            borrow = original < sub;
        }
        while (length > 1 && words[length - 1] == 0) --length;
    }
};

inline bool inInterval(uint64_t coefficient, int exponent, uint64_t lower, uint64_t upper,
                       int binaryExponent, bool inclusive) {
    Natural candidate(coefficient), low(lower), high(upper);
    if (exponent >= 0) candidate.power10(unsigned(exponent));
    else { low.power10(unsigned(-exponent)); high.power10(unsigned(-exponent)); }
    if (binaryExponent >= 0) { low.shift(unsigned(binaryExponent)); high.shift(unsigned(binaryExponent)); }
    else candidate.shift(unsigned(-binaryExponent));
    int lowerComparison = candidate.compare(low), upperComparison = candidate.compare(high);
    return (lowerComparison > 0 || (inclusive && lowerComparison == 0))
        && (upperComparison < 0 || (inclusive && upperComparison == 0));
}

inline unsigned render(uint64_t coefficient, int exponent, bool negative, char* output) {
    while (coefficient % 10 == 0) { coefficient /= 10; ++exponent; }
    char reversed[20], digits[20];
    unsigned count = 0;
    do { reversed[count++] = char('0' + coefficient % 10); coefficient /= 10; } while (coefficient);
    for (unsigned i = 0; i < count; ++i) digits[i] = reversed[count - i - 1];
    unsigned position = 0;
    if (negative) output[position++] = '-';
    int scientificExponent = int(count) + exponent - 1;
    if (scientificExponent >= -3 && scientificExponent < 7) {
        int point = int(count) + exponent;
        if (point <= 0) {
            output[position++] = '0'; output[position++] = '.';
            for (int i = 0; i < -point; ++i) output[position++] = '0';
            for (unsigned i = 0; i < count; ++i) output[position++] = digits[i];
        } else {
            for (int i = 0; i < point; ++i) output[position++] = i < int(count) ? digits[i] : '0';
            output[position++] = '.';
            if (point >= int(count)) output[position++] = '0';
            else for (unsigned i = unsigned(point); i < count; ++i) output[position++] = digits[i];
        }
    } else {
        output[position++] = digits[0]; output[position++] = '.';
        if (count == 1) output[position++] = '0';
        else for (unsigned i = 1; i < count; ++i) output[position++] = digits[i];
        output[position++] = 'E';
        if (scientificExponent < 0) { output[position++] = '-'; scientificExponent = -scientificExponent; }
        unsigned exponentDigits = 0;
        do { reversed[exponentDigits++] = char('0' + scientificExponent % 10); scientificExponent /= 10; }
        while (scientificExponent);
        while (exponentDigits) output[position++] = reversed[--exponentDigits];
    }
    output[position] = 0;
    return position;
}

// Select the shortest rounding decimal, closest to the binary value, ties to even.
inline unsigned format(uint64_t bits, unsigned fractionBits, unsigned exponentBits, int bias, char* output) {
    uint64_t fractionMask = (uint64_t(1) << fractionBits) - 1;
    uint64_t fraction = bits & fractionMask;
    unsigned encodedExponent = unsigned((bits >> fractionBits) & ((1u << exponentBits) - 1));
    bool negative = (bits >> (fractionBits + exponentBits)) != 0;
    if (encodedExponent == (1u << exponentBits) - 1) {
        const char* text = fraction ? "NaN" : (negative ? "-Infinity" : "Infinity");
        strcpy(output, text); return unsigned(strlen(text));
    }
    if (!encodedExponent && !fraction) {
        const char* text = negative ? "-0.0" : "0.0";
        strcpy(output, text); return unsigned(strlen(text));
    }
    uint64_t significand = fraction | (encodedExponent ? uint64_t(1) << fractionBits : 0);
    int binaryExponent = int(encodedExponent ? encodedExponent : 1) - bias - int(fractionBits);
    uint64_t lower = significand * 4 - (fraction == 0 && encodedExponent > 1 ? 1 : 2);
    uint64_t upper = significand * 4 + 2;
    Natural numerator(significand), denominator(1);
    if (binaryExponent >= 0) numerator.shift(unsigned(binaryExponent));
    else denominator.shift(unsigned(-binaryExponent));
    int exponent = 0;
    while (numerator.compare(denominator) < 0) { numerator.multiply(10); --exponent; }
    for (;;) {
        Natural next(denominator); next.multiply(10);
        if (numerator.compare(next) < 0) break;
        denominator = next; ++exponent;
    }
    uint64_t coefficient = 0;
    unsigned maximum = fractionBits == 23 ? 9 : 17;
    for (unsigned length = 1; length <= maximum; ++length) {
        unsigned digit = 0;
        while (numerator.compare(denominator) >= 0) { numerator.subtract(denominator); ++digit; }
        coefficient = coefficient * 10 + digit;
        if (length >= 2) {
            int decimalExponent = exponent - int(length) + 1;
            bool even = (significand & 1) == 0;
            bool floorValid = inInterval(coefficient, decimalExponent, lower, upper, binaryExponent - 2, even);
            bool ceilValid = inInterval(coefficient + 1, decimalExponent, lower, upper, binaryExponent - 2, even);
            if (floorValid || ceilValid) {
                Natural twice(numerator); twice.multiply(2);
                int rounding = twice.compare(denominator);
                bool up = !floorValid || (ceilValid && (rounding > 0 || (rounding == 0 && (coefficient & 1))));
                return render(coefficient + (up ? 1 : 0), decimalExponent, negative, output);
            }
        }
        numerator.multiply(10);
    }
    // All finite IEEE values have a rounding decimal within the digit limit.
    output[0] = 0;
    return 0;
}

} // namespace decimal
} // namespace shim

#endif
