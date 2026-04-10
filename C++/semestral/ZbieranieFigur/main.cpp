#include <iostream>
#include <vector>
#include <conio.h>    // For _kbhit() and _getch()
#include <windows.h>  // For Sleep() and SetConsoleCursorPosition
#include <cstdlib>
#include <ctime>
#include <algorithm>
#include <limits>

// Symbols for rendering
const char EMPTY = '.';
const char PLAYER_CHAR = 'P';
const char BOT_CHAR = 'B';
const char FOOD_CHAR = 'F'; // Single symbol for food

// Structure for position on the field
struct Position {
    int x;
    int y;

    bool operator==(const Position& other) const {
        return x == other.x && y == other.y;
    }
};

// Function to clear the screen
void clearScreen() {
    HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
    CONSOLE_SCREEN_BUFFER_INFO csbi;
    DWORD cellCount;
    DWORD count;
    COORD homeCoords = {0, 0};

    if (hConsole == INVALID_HANDLE_VALUE) return;

    // Get console screen buffer info
    if (!GetConsoleScreenBufferInfo(hConsole, &csbi)) return;
    cellCount = csbi.dwSize.X * csbi.dwSize.Y;

    // Fill the console with spaces
    if (!FillConsoleOutputCharacter(hConsole, (TCHAR) ' ', cellCount, homeCoords, &count)) return;

    // Fill the console with current text attributes
    if (!FillConsoleOutputAttribute(hConsole, csbi.wAttributes, cellCount, homeCoords, &count)) return;

    // Move the cursor to the top-left corner
    SetConsoleCursorPosition(hConsole, homeCoords);
}

// Game class
class Game {
public:
    Game();
    void run();

private:
    void initialize();
    void reset();
    void render();
    void placeFood();
    bool movePlayer(char direction);
    void moveBot();
    Position findNearestFood(Position botPos);
    bool isFoodAt(Position pos);
    void collectFood(Position pos, char collector); // Passed by value
    bool allFoodCollected();
    void displayInstructions();
    int getValidatedInput(const std::string& prompt, int min, int max);

    // Game field
    std::vector<std::vector<char>> field;

    // Dimensions of the game field
    int WIDTH;
    int HEIGHT;

    // Positions of the player and the bot
    Position playerPos;
    Position botPos;

    // Number of collected food items
    int playerScore;
    int botScore;

    // Total number of food items on the field
    int foodCount;

    // Console colors
    WORD COLOR_PLAYER;
    WORD COLOR_BOT;
    WORD COLOR_DEFAULT;
};

// Constructor
Game::Game() : playerScore(0), botScore(0), foodCount(0), WIDTH(10), HEIGHT(10) { // Initial values
    // Define colors
    COLOR_PLAYER = FOREGROUND_GREEN | FOREGROUND_INTENSITY;
    COLOR_BOT = FOREGROUND_RED | FOREGROUND_INTENSITY;
    COLOR_DEFAULT = FOREGROUND_RED | FOREGROUND_GREEN | FOREGROUND_BLUE;

    displayInstructions();
    // Prompt for field dimensions
    WIDTH = getValidatedInput("Enter the width of the playing field (minimum 5): ", 5, 100);
    HEIGHT = getValidatedInput("Enter the height of the playing field (minimum 5): ", 5, 100);

    initialize();
}

// Function to display instructions
void Game::displayInstructions() {
    std::cout << "============================\n";
    std::cout << "        WELCOME TO GAME      \n";
    std::cout << "============================\n\n";
    std::cout << "This game allows you to move the player (" << PLAYER_CHAR << ") around the playing field, collecting food (" << FOOD_CHAR << ").\n";
    std::cout << "The bot (" << BOT_CHAR << ") will also collect food automatically. Whoever collects the most food wins!\n\n";
    std::cout << "Controls:\n";
    std::cout << "  W - Up\n";
    std::cout << "  A - Left\n";
    std::cout << "  S - Down\n";
    std::cout << "  D - Right\n";
    std::cout << "  R - Restart the game\n";
    std::cout << "  Q - Exit\n\n";
}

// Function to get validated input from the user
int Game::getValidatedInput(const std::string& prompt, int min, int max) {
    int value;
    while (true) {
        std::cout << prompt;
        std::cin >> value;

        if(std::cin.fail()) { // Check for invalid input type
            std::cin.clear(); // Clear error state
            std::cin.ignore(std::numeric_limits<std::streamsize>::max(), '\n'); // Ignore invalid input
            std::cout << "Please enter a valid number.\n";
            continue;
        }

        if(value < min || value > max) {
            std::cout << "Please enter a number between " << min << " and " << max << ".\n";
            continue;
        }

        std::cin.ignore(std::numeric_limits<std::streamsize>::max(), '\n'); // Ignore remaining input
        break;
    }
    return value;
}

// Function to initialize the game
void Game::initialize() {
    srand(static_cast<unsigned>(time(nullptr)));

    // Initialize the game field
    field = std::vector<std::vector<char>>(HEIGHT, std::vector<char>(WIDTH, EMPTY));

    // Place the player in the center
    playerPos = { WIDTH / 2, HEIGHT / 2 };
    field[playerPos.y][playerPos.x] = PLAYER_CHAR;

    // Place the bot in the top-left corner (0,0)
    botPos = { 0, 0 };
    field[botPos.y][botPos.x] = BOT_CHAR;

    // Place food
    int totalFood = (WIDTH * HEIGHT) / 10; // For example, 10% of the field
    for(int i = 0; i < totalFood; ++i) {
        placeFood();
    }

    std::cout << "Initialization complete. Total food: " << foodCount << "\n\n";
}

// Function to reset the game
void Game::reset() {
    // Clear the field
    for(int y = 0; y < HEIGHT; ++y) {
        for(int x = 0; x < WIDTH; ++x) {
            field[y][x] = EMPTY;
        }
    }

    // Reset scores
    playerScore = 0;
    botScore = 0;
    foodCount = 0;

    // Place the player in the center
    playerPos = { WIDTH / 2, HEIGHT / 2 };
    field[playerPos.y][playerPos.x] = PLAYER_CHAR;

    // Place the bot in the top-left corner
    botPos = { 0, 0 };
    field[botPos.y][botPos.x] = BOT_CHAR;

    // Place food
    int totalFood = (WIDTH * HEIGHT) / 10; // For example, 10% of the field
    for(int i = 0; i < totalFood; ++i) {
        placeFood();
    }

    std::cout << "The game has been restarted.\n\n";
}

// Function to place food at a random position
void Game::placeFood() {
    Position pos;
    while(true) {
        pos.x = rand() % WIDTH;
        pos.y = rand() % HEIGHT;
        if(field[pos.y][pos.x] == EMPTY) {
            field[pos.y][pos.x] = FOOD_CHAR;
            foodCount++;
            break;
        }
    }
}

// Function to render the game field
void Game::render() {
    clearScreen();
    HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);

    for(int y = 0; y < HEIGHT; ++y) {
        for(int x = 0; x < WIDTH; ++x) {
            char current = field[y][x];
            if(current == PLAYER_CHAR) {
                SetConsoleTextAttribute(hConsole, COLOR_PLAYER);
                std::cout << current << ' ';
                SetConsoleTextAttribute(hConsole, COLOR_DEFAULT);
            }
            else if(current == BOT_CHAR) {
                SetConsoleTextAttribute(hConsole, COLOR_BOT);
                std::cout << current << ' ';
                SetConsoleTextAttribute(hConsole, COLOR_DEFAULT);
            }
            else if(current == FOOD_CHAR) {
                // Optionally: set a different color for food
                SetConsoleTextAttribute(hConsole, FOREGROUND_RED | FOREGROUND_GREEN | FOREGROUND_INTENSITY); // Yellow
                std::cout << current << ' ';
                SetConsoleTextAttribute(hConsole, COLOR_DEFAULT);
            }
            else {
                std::cout << current << ' ';
            }
        }
        std::cout << '\n';
    }
    std::cout << "Food collected: " << playerScore << " | Food collected by bot: " << botScore << '\n';
    std::cout << "(W/A/S/D for movement, R - restart, Q - exit)\n";

    std::cout.flush(); // Ensure all output is displayed
}

// Function to find the nearest food for the bot
Position Game::findNearestFood(Position botPos) {
    Position nearest = { -1, -1 };
    int minDist = WIDTH + HEIGHT;

    for(int y = 0; y < HEIGHT; ++y) {
        for(int x = 0; x < WIDTH; ++x) {
            if(field[y][x] == FOOD_CHAR) {
                int dist = abs(botPos.x - x) + abs(botPos.y - y);
                if(dist < minDist) {
                    minDist = dist;
                    nearest = { x, y };
                }
            }
        }
    }

    return nearest;
}

// Function to check if there is food at a given position
bool Game::isFoodAt(Position pos) {
    return field[pos.y][pos.x] == FOOD_CHAR;
}

// Function to collect food
void Game::collectFood(Position pos, char collector) { // Passed by value
    if(isFoodAt(pos)) {
        // Collect the food
        field[pos.y][pos.x] = collector;
        if(collector == PLAYER_CHAR)
            playerScore += 1; // Changed from 100 to 1
        else if(collector == BOT_CHAR)
            botScore += 1; // Changed from 100 to 1
        foodCount--;
    }
}

// Function to move the player
bool Game::movePlayer(char direction) {
    Position newPos = playerPos;
    switch(direction) {
        case 'w': newPos.y -= 1; break;
        case 's': newPos.y += 1; break;
        case 'a': newPos.x -= 1; break;
        case 'd': newPos.x += 1; break;
        default: return false;
    }

    // Check boundaries
    if(newPos.x < 0 || newPos.x >= WIDTH || newPos.y < 0 || newPos.y >= HEIGHT)
        return false;

    // Check if the bot is at the new position
    if(field[newPos.y][newPos.x] == BOT_CHAR)
        return false; // Cannot move onto the bot

    // Collect food if present
    collectFood(newPos, PLAYER_CHAR);

    // Update the field
    field[playerPos.y][playerPos.x] = EMPTY;
    playerPos = newPos;
    field[playerPos.y][playerPos.x] = PLAYER_CHAR;

    return true;
}

// Function to move the bot
void Game::moveBot() {
    if(foodCount == 0)
        return;

    Position target = findNearestFood(botPos);
    if(target.x == -1 && target.y == -1)
        return; // No food found

    // Determine the direction of movement
    int dx = target.x - botPos.x;
    int dy = target.y - botPos.y;

    Position newPos = botPos;

    if(abs(dx) > abs(dy)) {
        newPos.x += (dx > 0) ? 1 : -1;
    }
    else if(dy != 0) {
        newPos.y += (dy > 0) ? 1 : -1;
    }

    // Check boundaries
    if(newPos.x < 0 || newPos.x >= WIDTH || newPos.y < 0 || newPos.y >= HEIGHT)
        return;

    // Check if the player is at the new position
    if(field[newPos.y][newPos.x] == PLAYER_CHAR)
        return; // Cannot move onto the player

    // Collect food if present
    collectFood(newPos, BOT_CHAR);

    // Update the field
    field[botPos.y][botPos.x] = EMPTY;
    botPos = newPos;
    field[botPos.y][botPos.x] = BOT_CHAR;
}

// Function to check if all food has been collected
bool Game::allFoodCollected() {
    return foodCount == 0;
}

// Main game loop
void Game::run() {
    render();

    while(true) {
        // Check for input
        if(_kbhit()) {
            char ch = static_cast<char>(_getch());
            ch = static_cast<char>(tolower(ch));

            if(ch == 'q') {
                std::cout << "The game is over. The end.\n";
                break;
            }

            if(ch == 'r') {
                reset();
                render();
                continue; // Continue the loop after resetting
            }

            if(movePlayer(ch)) {
                render();

                if(allFoodCollected()) {
                    if(playerScore > botScore) {
                        std::cout << "You collected more food! You won!\n";
                    }
                    else if(playerScore < botScore) {
                        std::cout << "The bot collected more food! The bot won!\n";
                    }
                    else {
                        std::cout << "It's a draw! You and the bot collected the same amount of food.\n";
                    }

                    // Ask if the player wants to restart the game
                    std::cout << "Do you want to restart the game? (Y/N): ";
                    char response;
                    std::cin >> response;
                    if(tolower(response) == 'y') {
                        reset();
                        render();
                        continue;
                    } else {
                        break;
                    }
                }

                moveBot();
                render();

                if(allFoodCollected()) {
                    if(playerScore > botScore) {
                        std::cout << "You collected more food! You won!\n";
                    }
                    else if(playerScore < botScore) {
                        std::cout << "The bot collected more food! The bot won!\n";
                    }
                    else {
                        std::cout << "It's a draw! You and the bot collected the same amount of food.\n";
                    }

                    // Restart?
                    std::cout << "Do you want to restart the game? (Y/N): ";
                    char response;
                    std::cin >> response;
                    if(tolower(response) == 'y') {
                        reset();
                        render();
                        continue;
                    } else {
                        break;
                    }
                }
            }
        }

        // Delay to reduce CPU usage
        Sleep(100);
    }
}

int main() {
    Game game;
    game.run();
    return 0;
}
