import os

count = 0
for root, dirs, files in os.walk("../src"):
	for file in files:
		lines = len(open(root + '/' + file,'rU').readlines())
		count += lines
		print file + ': ' + str(lines)

print '\nTotal line number: ' + str(count)