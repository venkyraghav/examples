// https://projecteuler.net/problem=10
#[allow(dead_code)]
pub fn problem010(prime_below_sum: u64) -> u64 {
    let mut sum = 0;
    if prime_below_sum > 2 {
        (1..prime_below_sum).for_each(|n| {
            if is_prime(n) {
                sum += n;
            }
        });
    }
    println!("sum of primes below {}: {}", prime_below_sum, sum);
    sum
}

// https://projecteuler.net/problem=9
#[allow(dead_code)]
pub fn problem009(triplet_sum: u64) -> u64 {
    if triplet_sum > 0 {
        for a in 1..triplet_sum {
            for b in 1..triplet_sum {
                if a + b > triplet_sum {
                    break;
                }
                let c = triplet_sum - a - b;
                if a.pow(2) + b.pow(2) == c.pow(2) {
                    println!("a={}, b={}, c={}, product={}", a, b, c, a * b * c);
                    return a * b * c;
                }
            }
        }
    }
    0
}

// p>The four adjacent digits in the $1000$-digit number that have the greatest product are $9 \times 9 \times 8 \times 9 = 5832$.</p>
// <p class="monospace center">
// 73167176531330624919225119674426574742355349194934<br>
// 96983520312774506326239578318016984801869478851843<br>
// 85861560789112949495459501737958331952853208805511<br>
// 12540698747158523863050715693290963295227443043557<br>
// 66896648950445244523161731856403098711121722383113<br>
// 62229893423380308135336276614282806444486645238749<br>
// 30358907296290491560440772390713810515859307960866<br>
// 70172427121883998797908792274921901699720888093776<br>
// 65727333001053367881220235421809751254540594752243<br>
// 52584907711670556013604839586446706324415722155397<br>
// 53697817977846174064955149290862569321978468622482<br>
// 83972241375657056057490261407972968652414535100474<br>
// 82166370484403199890008895243450658541227588666881<br>
// 16427171479924442928230863465674813919123162824586<br>
// 17866458359124566529476545682848912883142607690042<br>
// 24219022671055626321111109370544217506941658960408<br>
// 07198403850962455444362981230987879927244284909188<br>
// 84580156166097919133875499200524063689912560717606<br>
// 05886116467109405077541002256983155200055935729725<br>
// 71636269561882670428252483600823257530420752963450<br></p>
// <p>Find the thirteen adjacent digits in the $1000$-digit number that have the greatest product. What is the value of this product?</p>
#[allow(dead_code)]
pub fn problem008(input: &str, num_digits: u8) -> u64 {
    let invalid_value = 12;
    if num_digits == 0 || input.is_empty() == true {
        return 0;
    }
    if input.len() < num_digits as usize {
        return 0;
    }

    let (mut max_product, mut curr_product) = (1 as u64, 1 as u64);
    let input_chars = input.chars();
    let mut curr_num = invalid_value;
    let mut cnt = 1;
    input_chars.for_each(|num_char| {
        if let Some(digit) = num_char.to_digit(10) {
            if digit == 0 {
                // Skip 0
                curr_num = invalid_value;
                curr_product = 1;
                cnt = 1;
            } else {
                // check for consecutive digits
                if digit + 1 == curr_num || digit - 1 == curr_num || digit == curr_num {
                    // curr_num = digit;
                    cnt += 1;
                    curr_product *= digit as u64;
                    if cnt == num_digits {
                        if curr_product > max_product {
                            max_product = curr_product;
                        }
                        cnt = 1;
                        curr_num = digit;
                        curr_product = digit as u64;
                    }
                } else {
                    // If not consecutive digits, also takes care of skipping 1st digit
                    cnt = 1;
                    curr_num = digit;
                    curr_product = digit as u64;
                }
            }
        }
    });
    println!(
        "Final: num_digits = {}, max_product = {}",
        num_digits, max_product
    );
    max_product
}

fn is_prime(num: u64) -> bool {
    if num <= 1 {
        return false;
    }
    if num <= 3 {
        return true;
    }
    if num % 2 == 0 || num % 3 == 0 {
        return false;
    }
    let mut i = 5;
    while i * i <= num {
        if num % i == 0 || num % (i + 2) == 0 {
            return false;
        }
        i += 6;
    }
    true
}

// <p>By listing the first six prime numbers: $2, 3, 5, 7, 11$, and $13$, we can see that the $6$th prime is $13$.</p>
// <p>What is the $10\,001$st prime number?</p>
#[allow(dead_code)]
pub fn problem007(num_primes: u32) -> u64 {
    let (mut prime_count, mut num_current) = (1 as u32, 2 as u64);

    while prime_count < num_primes {
        num_current += 1;
        if is_prime(num_current) {
            prime_count += 1;
            if prime_count == num_primes {
                break;
            }
        }
    }
    println!("{}th prime is {}", num_primes, num_current);
    num_current
}

// <p>The sum of the squares of the first ten natural numbers is,</p>
// $$1^2 + 2^2 + ... + 10^2 = 385.$$
// <p>The square of the sum of the first ten natural numbers is,</p>
// $$(1 + 2 + ... + 10)^2 = 55^2 = 3025.$$
// <p>Hence the difference between the sum of the squares of the first ten natural numbers and the square of the sum is $3025 - 385 = 2640$.</p>
// <p>Find the difference between the sum of the squares of the first one hundred natural numbers and the square of the sum.</p>
#[allow(dead_code)]
pub fn problem006(num: u64, power: u32) -> u64 {
    let sum_of_powers = (1..=num).map(|x| x.pow(power)).sum::<u64>();
    let powers_of_sum = (1..=num).sum::<u64>().pow(power);
    println!(
        "sum_of_powers: {}, powers_of_sum: {}, difference: {}",
        sum_of_powers,
        powers_of_sum,
        powers_of_sum - sum_of_powers
    );
    powers_of_sum - sum_of_powers
}

// <p>$2520$ is the smallest number that can be divided by each of the numbers from $1$ to $10$ without any remainder.</p>
// <p>What is the smallest positive number that is <strong class="tooltip">evenly divisible<span class="tooltiptext">divisible with no remainder</span></strong> by all of the numbers from $1$ to $20$?</p>
#[allow(dead_code)]
pub fn problem005(x: u64, y: u64) -> u64 {
    let mut result = 1;
    for i in x..=y {
        result = lcm(result, i);
    }
    println!("result {}", result);
    result
}

fn lcm(result: u64, i: u64) -> u64 {
    // println!("result {}, i {}", result, i);
    result * i / gcd(result, i)
}

fn gcd(a: u64, b: u64) -> u64 {
    if b == 0 { a } else { gcd(b, a % b) }
}

fn getmaxnum(num: u8) -> (u32, u32) {
    match num {
        2 => (10, 99),
        3 => (100, 999),
        4 => (1000, 9999),
        5 => (10000, 99999),
        6 => (100000, 999999),
        7 => (1000000, 9999999),
        _ => (0, 0),
    }
}

// A palindromic number reads the same both ways. The largest palindrome made from the product of two $2$-digit numbers is $9009 = 91 \times 99$
// Find the largest palindrome made from the product of two $3$-digit numbers.
#[allow(dead_code)]
pub fn problem004(num: u8) -> (u32, u32, u32) {
    if num > 7 {
        return (0, 0, 0);
    }
    let mut palindrome;
    let (mut x, maxx) = getmaxnum(num);
    let mut largestx = 0;
    let mut largesty = 0;
    let mut largestpalindrome = 0;
    while x <= maxx {
        let (mut y, maxy) = getmaxnum(num);
        while y <= maxy {
            palindrome = x * y;
            let reversed = palindrome
                .to_string()
                .as_mut_str()
                .chars()
                .rev()
                .collect::<String>();
            if reversed.eq(palindrome.to_string().as_mut_str()) {
                if palindrome > largestpalindrome {
                    largestpalindrome = palindrome;
                    (largestx, largesty) = (x, y);
                    println!(
                        "x: {}, y: {}, largestx: {}, largesty: {}, palindrome: {}, palindrome_reversed: {}",
                        x, y, largestx, largesty, palindrome, reversed
                    );
                }
            }
            y += 1;
        }
        x += 1;
    }
    if largestx > largesty {
        return (largesty, largestx, largestpalindrome);
    }
    (largestx, largesty, largestpalindrome)
}

/*
 * The prime factors of 13195 are 5, 7, 13 and 29.
 * What is the largest prime factor of the number?
 */
#[allow(dead_code)]
pub fn problem003(num: u64) -> u64 {
    let (mut largest, mut n, mut i) = (0, num, 2);
    while i * i <= n {
        while n % i == 0 {
            largest = i;
            n /= i;
        }
        i += 1;
    }
    if n > 1 {
        largest = n;
    }

    println!("Largest prime factor of {} is {}", num, largest);
    largest
}

// Each new term in the Fibonacci sequence is generated by adding the previous two terms. By starting with
// and , the first terms will be:
// By considering the terms in the Fibonacci sequence whose values do not exceed four million, find the sum of the even-valued terms.
#[allow(dead_code)]
pub fn problem002(max: u64) -> u64 {
    let (mut sum, mut n, mut nplus) = (0, 0, 1);
    while nplus < max {
        if nplus % 2 == 0 {
            sum += nplus;
        }
        (n, nplus) = (nplus, n + nplus);
    }
    // 4613732
    println!("The sum of all even fibonacci less than {} is {}", max, sum);
    sum
}

// If we list all the natural numbers below that are multiples of or, we get and.
// The sum of these multiples is .Find the sum of all the multiples of or below.
#[allow(dead_code)]
pub fn problem001() {
    let mut sum = 0;
    for i in 1..1000 {
        if i % 3 == 0 || i % 5 == 0 {
            sum += i;
        }
    }
    // 233168
    println!("The sum of all multiples of 3 or 5 below 1000 is {}", sum);
}

mod tests {
    #[allow(unused_imports)]
    use super::*;

    #[test]
    fn test_problem002() {
        assert_eq!(problem002(30), 10);
        assert_eq!(problem002(4_000_001), 4_613_732);
    }

    #[test]
    fn test_problem003() {
        assert_eq!(problem003(13_195), 29);
        assert_eq!(problem003(600_851_475_143), 6857);
    }

    #[test]
    fn test_problem004() {
        assert_eq!(problem004(2), (91, 99, 9_009));
        assert_eq!(problem004(3), (913, 993, 906_609));
        assert_eq!(problem004(8), (0, 0, 0));
    }

    #[test]
    fn test_problem005() {
        assert_eq!(problem005(1, 10), 2_520);
        assert_eq!(problem005(1, 20), 232_792_560);
        assert_eq!(problem005(1, 17), 12_252_240);
    }

    #[test]
    fn test_problem006() {
        assert_eq!(problem006(10, 2), 2_640);
        assert_eq!(problem006(100, 2), 25_164_150);
        assert_eq!(problem006(10, 3), 163_350);
        assert_eq!(problem006(100, 3), 128_762_122_500);
    }

    #[test]
    fn test_problem007() {
        assert_eq!(problem007(6), 13);
        assert_eq!(problem007(10_001), 104743);
    }

    #[test]
    fn test_problem008() {
        let input = r#"
73167176531330624919225119674426574742355349194934
96983520312774506326239578318016984801869478851843
85861560789112949495459501737958331952853208805511
12540698747158523863050715693290963295227443043557
66896648950445244523161731856403098711121722383113
62229893423380308135336276614282806444486645238749
30358907296290491560440772390713810515859307960866
70172427121883998797908792274921901699720888093776
65727333001053367881220235421809751254540594752243
52584907711670556013604839586446706324415722155397
53697817977846174064955149290862569321978468622482
83972241375657056057490261407972968652414535100474
82166370484403199890008895243450658541227588666881
16427171479924442928230863465674813919123162824586
17866458359124566529476545682848912883142607690042
24219022671055626321111109370544217506941658960408
07198403850962455444362981230987879927244284909188
845801561660979191338754992005240636899125607176060588611646710940507754100225698315520005593572972571636269561882670428252483600823257530420752963450"#;

        assert_eq!(problem008("", 4), 0);
        assert_eq!(problem008(input, 0), 0);
        assert_eq!(problem008("123456789", 13), 0);
        assert_eq!(problem008(input, 4), 5832);
        // assert_eq!(problem008(input, 13), 28224);
    }

    #[test]
    fn test_problem009() {
        assert_eq!(problem009(12), 3 * 4 * 5);
        assert_eq!(problem009(1_000), 31875000);
    }

    #[test]
    fn test_problem010() {
        assert_eq!(problem010(10), 17);
        assert_eq!(problem010(2_000_000), 142_913_828_922);
    }
}
