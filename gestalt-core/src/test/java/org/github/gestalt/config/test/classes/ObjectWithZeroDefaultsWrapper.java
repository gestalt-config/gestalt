package org.github.gestalt.config.test.classes;

public class ObjectWithZeroDefaultsWrapper {
    public Byte myByte = (byte) 0;
    public Short myShort = 0;
    public Integer myInteger = 0;
    public Long myLong = 0L;
    public Float myFloat = 0F;
    public Double myDouble = 0D;
    public Character myChar = Character.MIN_VALUE;
    public String myString = "";
    public Boolean myBoolean = false;

    public ObjectWithZeroDefaultsWrapper() {
    }
}
