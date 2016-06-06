import os

count = 0
max_file_name_length = 0
max_line_count_length = 0
file_dict = {}
print '\n===================== Files ====================='
for root, dirs, files in os.walk("../src"):
	for file in files:
		lines = len(open(root + '/' + file,'rU').readlines())
		max_file_name_length = max_file_name_length if max_file_name_length > len(file) else len(file) 
		max_line_count_length = max_line_count_length if max_line_count_length > len(str(lines)) else len(str(lines))
		
		count += lines
		# print '(' + file + ')' + ': ' + str(lines)
		file_dict[file] = {
			"lines": lines,
			"filename": file
		}

for key in file_dict:
	extra_space_count = max_file_name_length - len(file_dict[key]['filename'])
	extra_space = ''
	while extra_space_count > 0 :
		extra_space += ' '
		extra_space_count -= 1

	extra_space_for_line_count = max_line_count_length - len(str(file_dict[key]['lines']))
	extra_space_for_line = ''
	while extra_space_for_line_count > 0:
		extra_space_for_line += ' '
		extra_space_for_line_count -= 1
	
	print key + extra_space + ' : ' + str(file_dict[key]['lines']) + extra_space_for_line + ' [{0:.1%}]'.format(file_dict[key]['lines']/float(count))

print '\n\n===================== Total ====================='
print 'Total line number: ' + str(count) + '\n'