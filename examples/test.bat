java -jar -Duser.language=ru -Duser.region=RU libs/train-@project.version@.jar ^
--model input/model.xlsx ^
--testSetFile input/testSetReal.xlsx ^
--testOutputFile output/test_output.xlsx
pause