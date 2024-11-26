package com.example.demo.utils;

import java.util.function.Function;

public sealed interface Either<T, U> permits Either.Left, Either.Right {
  public static <T, U> Either<T, U> left(T value) {
    return Left.of(value);
  }

  public static <T, U> Either<T, U> right(U value) {
    return Right.of(value);
  }

  <S> S either(
      Function<? super T, ? extends S> leftMapper, Function<? super U, ? extends S> rightMapper);

  public record Left<T, U>(T value) implements Either<T, U> {
    public static <T, U> Left<T, U> of(T value) {
      return new Left<>(value);
    }

    @Override
    public <S> S either(
        Function<? super T, ? extends S> leftMapper, Function<? super U, ? extends S> rightMapper) {
      return leftMapper.apply(value);
    }
  }

  public record Right<T, U>(U value) implements Either<T, U> {
    public static <T, U> Right<T, U> of(U value) {
      return new Right<>(value);
    }

    @Override
    public <S> S either(
        Function<? super T, ? extends S> leftMapper, Function<? super U, ? extends S> rightMapper) {
      return rightMapper.apply(value);
    }
  }
}
