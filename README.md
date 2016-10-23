This codebase recreates items from _Code: The Hidden Language of Computer Hardware and Software_ by Charles Petzold. The
focus is on creating the computer described in chapter 17, while simulating each component down to the individual bits.

**Highlights**

The `Signals` file contains the low-level framework powering everything. The `Computer` class contains the final computer
implementation. The `ComputerMultiplyTest` demonstrates a modified version of the multiplication program described on
pages 228-230 of _Code_, and that code is also present in `Main.kt` of the demo module to demonstrate usage and provide 
a template for client code using these classes.