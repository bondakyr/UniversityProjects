package cz.cvut.fel.pjv.impl;

import cz.cvut.fel.pjv.Thief;

public class BruteForceAttacker extends Thief {

    @Override
    public void breakPassword(int sizeOfPassword) {
        char[] characters = getCharacters();
        char[] password = new char[sizeOfPassword];
        generatePasswords(characters, password, sizeOfPassword, 0);
    }

    private boolean generatePasswords(char[] characters, char[] password, int sizeOfPassword, int index) {
        if (index == sizeOfPassword) {
            return tryOpen(password);
        }

        for (char character : characters) {
            password[index] = character;
            if (generatePasswords(characters, password, sizeOfPassword, index + 1)) {
                return true;
            }
        }
        return false;
    }
}