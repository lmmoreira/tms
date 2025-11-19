package br.com.logistics.tms.integration.data;

import java.util.concurrent.ThreadLocalRandom;

public final class CnpjGenerator {

    private CnpjGenerator() {
    }

    public static String randomCnpj() {
        final int[] base = new int[12];
        final ThreadLocalRandom rand = ThreadLocalRandom.current();
        for (int i = 0; i < base.length; i++) {
            base[i] = rand.nextInt(0, 10);
        }

        final int firstVerifier = computeVerifierDigit(base, new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});
        final int[] withFirst = new int[13];
        System.arraycopy(base, 0, withFirst, 0, base.length);
        withFirst[12] = firstVerifier;
        final int secondVerifier = computeVerifierDigit(withFirst, new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});

        final StringBuilder digits = new StringBuilder(14);
        for (int d : base) digits.append(d);
        digits.append(firstVerifier).append(secondVerifier);

        return formatCnpj(digits.toString());
    }

    private static int computeVerifierDigit(final int[] numbers, final int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += numbers[i] * weights[i];
        }
        final int mod = sum % 11;
        return mod < 2 ? 0 : 11 - mod;
    }

    private static String formatCnpj(final String digits14) {
        return digits14.substring(0, 2) + '.' +
                digits14.substring(2, 5) + '.' +
                digits14.substring(5, 8) + '/' +
                digits14.substring(8, 12) + '-' +
                digits14.substring(12, 14);
    }
}
